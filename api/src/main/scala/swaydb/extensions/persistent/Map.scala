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

package swaydb.extensions.persistent

import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import swaydb.{IO, SwayDB}
import swaydb.configs.level.{DefaultGroupingStrategy, DefaultPersistentConfig}
import swaydb.core.BlockingCore
import swaydb.core.function.FunctionStore
import swaydb.data.accelerate.{Accelerator, LevelZeroMeter}
import swaydb.data.api.grouping.KeyValueGroupingStrategy
import swaydb.data.config._
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
    * A pre-configured, 8 Leveled, persistent database where Level1 accumulates a minimum of 10 Segments before
    * pushing Segments to lower Level.
    *
    * For custom configurations read documentation on website: http://www.swaydb.io/configuring-levels
    *
    * @param dir                         Root directory for all Level where appendix folder & files are created
    * @param otherDirs                   Secondary directories for all Levels where Segments get distributed.
    * @param maxOpenSegments             Number of concurrent Segments opened
    * @param cacheSize                   Size of in-memory key-values
    * @param mapSize                     Size of LevelZero's maps (WAL)
    * @param mmapMaps                    Memory-maps LevelZero maps files if set to true else reverts java.nio.FileChannel
    * @param mmapAppendix                Memory-maps Levels appendix files if set to true else reverts java.nio.FileChannel
    * @param mmapSegments                Memory-maps Levels Segment files if set to true else reverts java.nio.FileChannel
    * @param segmentSize                 Minimum size of Segment files in each Level
    * @param appendixFlushCheckpointSize Size of the appendix file before it's flushed. Appendix files are append only log files.
    *                                    Flushing removes deleted entries in the file hence reducing the size of the file.
    * @param cacheCheckDelay             Sets the max interval at which key-values get dropped from the cache. The delays
    *                                    are dynamically adjusted based on the current size of the cache to stay close the set
    *                                    cacheSize.
    * @param segmentsOpenCheckDelay      Sets the max interval at which Segments get closed. The delays
    *                                    are dynamically adjusted based on the current number of open Segments.
    * @param acceleration                Controls the write speed.
    * @param keySerializer               Converts keys to Bytes
    * @param valueSerializer             Converts values to Bytes
    * @param keyOrder                    Sort order for keys
    * @param ec                          ExecutionContext
    * @tparam K Type of key
    * @tparam V Type of value
    *
    * @return Database instance
    */

  def apply[K, V](dir: Path,
                  maxOpenSegments: Int = 1000,
                  cacheSize: Int = 100.mb,
                  mapSize: Int = 4.mb,
                  mmapMaps: Boolean = true,
                  recoveryMode: RecoveryMode = RecoveryMode.ReportFailure,
                  mmapAppendix: Boolean = true,
                  mmapSegments: MMAP = MMAP.WriteAndRead,
                  segmentSize: Int = 2.mb,
                  appendixFlushCheckpointSize: Int = 2.mb,
                  otherDirs: Seq[Dir] = Seq.empty,
                  cacheCheckDelay: FiniteDuration = 10.seconds,
                  segmentsOpenCheckDelay: FiniteDuration = 10.seconds,
                  mightContainFalsePositiveRate: Double = 0.01,
                  compressDuplicateValues: Boolean = true,
                  deleteSegmentsEventually: Boolean = false,
                  lastLevelGroupingStrategy: Option[KeyValueGroupingStrategy] = Some(DefaultGroupingStrategy()),
                  acceleration: LevelZeroMeter => Accelerator = Accelerator.noBrakes())(implicit keySerializer: Serializer[K],
                                                                                        valueSerializer: Serializer[V],
                                                                                        keyOrder: KeyOrder[Slice[Byte]] = KeyOrder.default,
                                                                                        fileOpenLimiterEC: ExecutionContext = SwayDB.defaultExecutionContext,
                                                                                        cacheLimiterEC: ExecutionContext = SwayDB.defaultExecutionContext): IO[swaydb.extensions.Map[K, V]] =
    BlockingCore(
      config = DefaultPersistentConfig(
        dir = dir,
        otherDirs = otherDirs,
        mapSize = mapSize, mmapMaps = mmapMaps,
        mmapSegments = mmapSegments,
        recoveryMode = recoveryMode,
        mmapAppendix = mmapAppendix,
        segmentSize = segmentSize,
        appendixFlushCheckpointSize = appendixFlushCheckpointSize,
        mightContainFalsePositiveRate = mightContainFalsePositiveRate,
        compressDuplicateValues = compressDuplicateValues,
        deleteSegmentsEventually = deleteSegmentsEventually,
        keyValueGroupingStrategy = lastLevelGroupingStrategy,
        acceleration = acceleration
      ),
      maxOpenSegments = maxOpenSegments,
      cacheSize = cacheSize,
      cacheCheckDelay = cacheCheckDelay,
      segmentsOpenCheckDelay = segmentsOpenCheckDelay,
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
