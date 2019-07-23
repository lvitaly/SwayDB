/*
 * Copyright (c) 2019 Simer Plaha (@simerplaha)
 *
 * This file is a part of SwayDB.
 *
 * SwayDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * SwayDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with SwayDB. If not, see <https://www.gnu.org/licenses/>.
 */

package swaydb.extensions.memory

import com.typesafe.scalalogging.LazyLogging
import swaydb.{IO, SwayDB}
import swaydb.configs.level.DefaultMemoryConfig
import swaydb.core.BlockingCore
import swaydb.core.function.FunctionStore
import swaydb.data.accelerate.{Accelerator, LevelZeroMeter}
import swaydb.data.api.grouping.KeyValueGroupingStrategy
import swaydb.data.order.{KeyOrder, TimeOrder}
import swaydb.data.slice.Slice
import swaydb.data.util.StorageUnits._
import swaydb.extensions.{Extend, Key}
import swaydb.serializers.Serializer

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, _}

object Map extends LazyLogging {

  implicit val timeOrder: TimeOrder[Slice[Byte]] = TimeOrder.long
  implicit val functionStore: FunctionStore = FunctionStore.memory()

  /**
    * A 2 Leveled (Level0 & Level1), in-memory database.
    *
    * For custom configurations read documentation on website: http://www.swaydb.io/configuring-levels
    *
    * @param mapSize           size of Level0 maps before they are converted into Segments
    * @param segmentSize       size of Level1 Segments
    * @param acceleration      Controls the write speed.
    * @param keySerializer     Converts keys to Bytes
    * @param valueSerializer   Converts values to Bytes
    * @param keyOrder          Sort order for keys
    * @param fileOpenLimiterEC Execution context used to close opened files when the maxOpenFiles limit is reached.
    * @param cacheLimiterEC    Execution context used to drop cached key-values when cacheSize is reached.
    * @tparam K
    * @tparam V
    *
    * @return
    */

  def apply[K, V](mapSize: Int = 4.mb,
                  segmentSize: Int = 2.mb,
                  cacheSize: Int = 500.mb,
                  cacheCheckDelay: FiniteDuration = 10.seconds,
                  mightContainFalsePositiveRate: Double = 0.01,
                  compressDuplicateValues: Boolean = false,
                  deleteSegmentsEventually: Boolean = false,
                  groupingStrategy: Option[KeyValueGroupingStrategy] = None,
                  acceleration: LevelZeroMeter => Accelerator = Accelerator.noBrakes())(implicit keySerializer: Serializer[K],
                                                                                        valueSerializer: Serializer[V],
                                                                                        keyOrder: KeyOrder[Slice[Byte]] = KeyOrder.default,
                                                                                        fileOpenLimiterEC: ExecutionContext = SwayDB.defaultExecutionContext,
                                                                                        cacheLimiterEC: ExecutionContext = SwayDB.defaultExecutionContext): IO[swaydb.extensions.Map[K, V]] =
    BlockingCore(
      config = DefaultMemoryConfig(
        mapSize = mapSize,
        segmentSize = segmentSize,
        mightContainFalsePositiveRate = mightContainFalsePositiveRate,
        compressDuplicateValues = compressDuplicateValues,
        deleteSegmentsEventually = deleteSegmentsEventually,
        keyValueGroupingStrategy = groupingStrategy,
        acceleration = acceleration
      ),
      maxOpenSegments = 0,
      cacheSize = cacheSize,
      cacheCheckDelay = cacheCheckDelay,
      //memory Segments are never closed.
      segmentsOpenCheckDelay = Duration.Zero,
      fileOpenLimiterEC = fileOpenLimiterEC,
      cacheLimiterEC = cacheLimiterEC
    ) flatMap {
      db =>
        implicit val optionValueSerializer: Serializer[Option[V]] =
          new Serializer[Option[V]] {
            override def write(data: Option[V]): Slice[Byte] =
              data.map(valueSerializer.write).getOrElse(Slice.emptyBytes)

            override def read(data: Slice[Byte]): Option[V] =
              if (data.isEmpty)
                None
              else
                Some(valueSerializer.read(data))
          }

        val map = swaydb.Map[Key[K], Option[V], IO](db)
        Extend(map = map)(
          keySerializer = keySerializer,
          optionValueSerializer = optionValueSerializer,
          keyOrder = Key.ordering(keyOrder)
        )
    }
}
