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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with SwayDB. If not, see <https://www.gnu.org/licenses/>.
 */

package swaydb.java.memory

import java.util.Comparator
import java.util.concurrent.ExecutorService

import swaydb.data.accelerate.{Accelerator, LevelZeroMeter}
import swaydb.data.order.KeyOrder
import swaydb.data.slice.Slice
import swaydb.data.util.StorageUnits._
import swaydb.java.data.slice.ByteSlice
import swaydb.java.data.util.Java.{JavaFunction, _}
import swaydb.java.serializers.{SerializerConverter, Serializer => JavaSerializer}
import swaydb.java.{IO, KeyOrderConverter, PureFunction, Return}
import swaydb.serializers.Serializer
import swaydb.{Apply, SwayDB, Tag}

import scala.beans.{BeanProperty, BooleanBeanProperty}
import scala.compat.java8.DurationConverters._
import scala.compat.java8.FunctionConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.reflect.ClassTag

object Set {

  class Config[A, F <: swaydb.java.PureFunction.OnKey[A, Void, Return.Set[Void]], SF](@BeanProperty var mapSize: Int = 4.mb,
                                                                                      @BeanProperty var segmentSize: Int = 2.mb,
                                                                                      @BeanProperty var maxOpenSegments: Int = 100,
                                                                                      @BeanProperty var maxCachedKeyValuesPerSegment: Int = 10,
                                                                                      @BeanProperty var fileSweeperPollInterval: java.time.Duration = 10.seconds.toJava,
                                                                                      @BeanProperty var mightContainFalsePositiveRate: Double = 0.01,
                                                                                      @BooleanBeanProperty var deleteSegmentsEventually: Boolean = true,
                                                                                      @BeanProperty var acceleration: JavaFunction[LevelZeroMeter, Accelerator] = (Accelerator.noBrakes() _).asJava,
                                                                                      @BeanProperty var comparator: IO[Comparator[ByteSlice], Comparator[A]] = IO.leftNeverException[Comparator[ByteSlice], Comparator[A]](swaydb.java.SwayDB.defaultComparator),
                                                                                      @BeanProperty var fileSweeperExecutorService: ExecutorService = SwayDB.defaultExecutorService,
                                                                                      serializer: Serializer[A],
                                                                                      functionClassTag: ClassTag[SF]) {

    implicit def scalaKeyOrder: KeyOrder[Slice[Byte]] = KeyOrderConverter.toScalaKeyOrder(comparator, serializer)

    implicit def fileSweeperEC: ExecutionContext = fileSweeperExecutorService.asScala

    def init(): IO[Throwable, swaydb.java.SetIO[A, F]] =
      new IO(
        swaydb.IO {
          val scalaMap =
            swaydb.memory.Set[A, SF, swaydb.IO.ThrowableIO](
              mapSize = mapSize,
              segmentSize = segmentSize,
              maxOpenSegments = maxOpenSegments,
              maxCachedKeyValuesPerSegment = maxCachedKeyValuesPerSegment,
              fileSweeperPollInterval = fileSweeperPollInterval.toScala,
              mightContainFalsePositiveRate = mightContainFalsePositiveRate,
              deleteSegmentsEventually = deleteSegmentsEventually,
              acceleration = acceleration.asScala
            )(serializer = serializer,
              functionClassTag = functionClassTag,
              tag = Tag.throwableIO,
              keyOrder = Left(scalaKeyOrder),
              fileSweeperEC = fileSweeperEC
            ).get

          swaydb.java.SetIO[A, F](scalaMap)
        }
      )(IO.throwableExceptionHandler)
  }

  def configWithFunctions[A](serializer: JavaSerializer[A]): Config[A, swaydb.java.PureFunction.OnKey[A, Void, Return.Set[Void]], swaydb.PureFunction.OnKey[A, Void, Apply.Set[Void]]] =
    new Config(
      serializer = SerializerConverter.toScala(serializer),
      functionClassTag = ClassTag.Any.asInstanceOf[ClassTag[swaydb.PureFunction.OnKey[A, Void, Apply.Set[Void]]]]
    )

  def config[A](serializer: JavaSerializer[A]): Config[A, PureFunction.VoidS[A], Void] =
    new Config[A, PureFunction.VoidS[A], Void](
      serializer = SerializerConverter.toScala(serializer),
      functionClassTag = ClassTag.Nothing.asInstanceOf[ClassTag[Void]]
    )

}
