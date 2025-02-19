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

package swaydb.core

import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

import org.scalatest.Matchers._
import swaydb.Error.Segment.ExceptionHandler
import swaydb.IO.ExceptionHandler.Nothing
import swaydb.IOValues._
import swaydb.compression.CompressionInternal
import swaydb.core.CommonAssertions._
import swaydb.core.TestSweeper.fileSweeper
import swaydb.core.actor.{FileSweeper, MemorySweeper}
import swaydb.core.cache.Cache
import swaydb.core.data.KeyValue.ReadOnly
import swaydb.core.data.Transient.Range
import swaydb.core.data.Value.{FromValue, RangeValue}
import swaydb.core.data._
import swaydb.core.function.FunctionStore
import swaydb.core.io.file.{BlockCache, Effect}
import swaydb.core.level.seek._
import swaydb.core.level.zero.LevelZero
import swaydb.core.level.{Level, NextLevel}
import swaydb.core.map.serializer.RangeValueSerializer
import swaydb.core.segment.format.a.block._
import swaydb.core.segment.format.a.block.binarysearch.{BinarySearchIndexBlock, BinarySearchEntryFormat}
import swaydb.core.segment.format.a.block.hashindex.{HashIndexBlock, HashIndexEntryFormat}
import swaydb.core.segment.format.a.block.reader.{BlockedReader, UnblockedReader}
import swaydb.core.segment.format.a.entry.id.BaseEntryIdFormatA
import swaydb.core.segment.{ReadState, Segment}
import swaydb.core.util.{BlockCacheFileIDGenerator, IDGenerator}
import swaydb.data.MaxKey
import swaydb.data.accelerate.Accelerator
import swaydb.data.compaction.{LevelMeter, Throttle}
import swaydb.data.config.{ActorConfig, Dir, IOStrategy, RecoveryMode}
import swaydb.data.order.{KeyOrder, TimeOrder}
import swaydb.data.slice.Slice
import swaydb.data.storage.{AppendixStorage, Level0Storage, LevelStorage}
import swaydb.data.util.StorageUnits._
import swaydb.serializers.Default._
import swaydb.serializers._
import swaydb.{IO, Scheduler}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.util.Random

object TestData {

  /**
   * Sequential time bytes generator.
   */

  implicit val scheduler = Scheduler()(TestExecutionContext.executionContext)

  val allBaseEntryIds = BaseEntryIdFormatA.baseIds

  implicit val functionStore: FunctionStore = FunctionStore.memory()

  val functionIdGenerator = new AtomicInteger(0)

  implicit def toMemory(slice: Slice[Transient])(implicit keyOrder: KeyOrder[Slice[Byte]]) = slice.toMemory

  def randomNextInt(max: Int): Int =
    Math.abs(Random.nextInt(max))

  def randomBoolean(): Boolean =
    Random.nextBoolean()

  implicit class KeyValuesImplicits(keyValues: Iterable[Transient]) {
    def updateStats: Slice[Transient] =
      updateStats(
        valuesConfig = keyValues.last.valuesConfig,
        sortedIndexConfig = keyValues.last.sortedIndexConfig,
        binarySearchIndexConfig = keyValues.last.binarySearchIndexConfig,
        hashIndexConfig = keyValues.last.hashIndexConfig,
        bloomFilterConfig = keyValues.last.bloomFilterConfig
      )

    def updateStats(valuesConfig: ValuesBlock.Config = keyValues.last.valuesConfig,
                    sortedIndexConfig: SortedIndexBlock.Config = keyValues.last.sortedIndexConfig,
                    binarySearchIndexConfig: BinarySearchIndexBlock.Config = keyValues.last.binarySearchIndexConfig,
                    hashIndexConfig: HashIndexBlock.Config = keyValues.last.hashIndexConfig,
                    bloomFilterConfig: BloomFilterBlock.Config = keyValues.last.bloomFilterConfig) = {
      val slice = Slice.create[Transient](keyValues.size)
      keyValues foreach {
        keyValue =>
          slice.add(
            keyValue.updatePrevious(
              valuesConfig = valuesConfig,
              sortedIndexConfig = sortedIndexConfig,
              binarySearchIndexConfig = binarySearchIndexConfig,
              hashIndexConfig = hashIndexConfig,
              bloomFilterConfig = bloomFilterConfig,
              previous = slice.lastOption
            )
          )
      }
      slice
    }
  }

  implicit class ReopenSegment(segment: Segment)(implicit keyOrder: KeyOrder[Slice[Byte]] = KeyOrder.default,
                                                 ec: ExecutionContext,
                                                 keyValueMemorySweeper: Option[MemorySweeper.KeyValue] = TestSweeper.memorySweeperMax,
                                                 fileSweeper: FileSweeper.Enabled = fileSweeper,
                                                 timeOrder: TimeOrder[Slice[Byte]] = TimeOrder.long,
                                                 blockCache: Option[BlockCache.State] = TestSweeper.randomBlockCache,
                                                 segmentIO: SegmentIO = SegmentIO.random) {

    def tryReopen: Segment =
      tryReopen(segment.path)

    def tryReopen(path: Path): Segment = {
      val reopenedSegment =
        Segment(
          path = path,
          segmentId = Effect.fileId(path)._1,
          mmapReads = randomBoolean(),
          mmapWrites = randomBoolean(),
          blockCacheFileId = BlockCacheFileIDGenerator.nextID,
          minKey = segment.minKey,
          maxKey = segment.maxKey,
          segmentSize = segment.segmentSize,
          minMaxFunctionId = segment.minMaxFunctionId,
          nearestExpiryDeadline = segment.nearestExpiryDeadline
        )

      segment.close
      reopenedSegment
    }

    def reopen: Segment =
      tryReopen.runRandomIO.right.value

    def reopen(path: Path): Segment =
      tryReopen(path).runRandomIO.right.value

    def get(key: Slice[Byte]): Option[ReadOnly] =
      segment.get(key, ReadState.random)

    def get(key: Int): Option[ReadOnly] =
      segment.get(key, ReadState.random)

    def higher(key: Int): Option[ReadOnly] =
      segment.higher(key, ReadState.random)

    def higher(key: Slice[Byte]): Option[ReadOnly] =
      segment.higher(key, ReadState.random)

    def lower(key: Int): Option[ReadOnly] =
      segment.lower(key, ReadState.random)

    def lower(key: Slice[Byte]): Option[ReadOnly] =
      segment.lower(key, ReadState.random)
  }

  implicit class ReopenLevel(level: Level)(implicit keyOrder: KeyOrder[Slice[Byte]] = KeyOrder.default,
                                           ec: ExecutionContext,
                                           timeOrder: TimeOrder[Slice[Byte]] = TimeOrder.long,
                                           keyValueMemorySweeper: Option[MemorySweeper.KeyValue] = TestSweeper.memorySweeperMax,
                                           segmentIO: SegmentIO = SegmentIO.random) {

    import swaydb.Error.Level.ExceptionHandler
    import swaydb.IO._

    //This test function is doing too much. This shouldn't be the case! There needs to be an easier way to write
    //key-values in a Level without that level copying it forward to lower Levels.
    def putKeyValuesTest(keyValues: Slice[KeyValue.ReadOnly])(implicit fileSweeper: FileSweeper.Enabled = TestSweeper.fileSweeper,
                                                              blockCache: Option[BlockCache.State] = TestSweeper.randomBlockCache): IO[swaydb.Error.Level, Unit] = {
      def fetchNextPath = {
        val segmentId = level.segmentIDGenerator.nextID
        val path = level.paths.next.resolve(IDGenerator.segmentId(segmentId))
        (segmentId, path)
      }

      if (keyValues.isEmpty)
        IO.unit
      else if (!level.isEmpty)
        level.putKeyValues(keyValues, level.segmentsInLevel(), None)
      else if (level.inMemory)
        IO {
          Segment.copyToMemory(
            keyValues = keyValues,
            fetchNextPath = fetchNextPath,
            removeDeletes = false,
            minSegmentSize = 1000.mb,
            createdInLevel = level.levelNumber,
            valuesConfig = level.valuesConfig,
            sortedIndexConfig = level.sortedIndexConfig,
            binarySearchIndexConfig = level.binarySearchIndexConfig,
            hashIndexConfig = level.hashIndexConfig,
            bloomFilterConfig = level.bloomFilterConfig
          )
        } flatMap {
          segments =>
            segments should have size 1
            segments mapRecoverIO {
              segment =>
                level.putKeyValues(keyValues.toMemory, Seq(segment), None)
            } map {
              _ => ()
            }
        }
      else
        IO {
          Segment.copyToPersist(
            keyValues = keyValues.toTransient(),
            createdInLevel = level.levelNumber,
            fetchNextPath = fetchNextPath,
            mmapSegmentsOnRead = randomBoolean(),
            mmapSegmentsOnWrite = randomBoolean(),
            removeDeletes = false,
            minSegmentSize = 1000.mb,
            segmentConfig = level.segmentConfig,
            valuesConfig = level.valuesConfig,
            sortedIndexConfig = level.sortedIndexConfig,
            binarySearchIndexConfig = level.binarySearchIndexConfig,
            hashIndexConfig = level.hashIndexConfig,
            bloomFilterConfig = level.bloomFilterConfig
          )
        } flatMap {
          segments =>
            segments should have size 1
            segments mapRecoverIO {
              segment =>
                level.putKeyValues(keyValues.toMemory, Seq(segment), None)
            } map {
              _ => ()
            }
        }
    }

    def reopen: Level =
      reopen()

    def tryReopen: IO[swaydb.Error.Level, Level] =
      tryReopen()

    def reopen(segmentSize: Long = level.segmentSize,
               throttle: LevelMeter => Throttle = level.throttle,
               nextLevel: Option[NextLevel] = level.nextLevel)(implicit keyValueMemorySweeper: Option[MemorySweeper.KeyValue] = TestSweeper.memorySweeperMax,
                                                               fileSweeper: FileSweeper = fileSweeper): Level =
      tryReopen(
        segmentSize = segmentSize,
        throttle = throttle,
        nextLevel = nextLevel
      ).right.value

    def tryReopen(segmentSize: Long = level.segmentSize,
                  throttle: LevelMeter => Throttle = level.throttle,
                  nextLevel: Option[NextLevel] = level.nextLevel)(implicit keyValueMemorySweeper: Option[MemorySweeper.KeyValue] = TestSweeper.memorySweeperMax,
                                                                  fileSweeper: FileSweeper.Enabled = fileSweeper,
                                                                  blockCache: Option[BlockCache.State] = TestSweeper.randomBlockCache): IO[swaydb.Error.Level, Level] =
      level.releaseLocks flatMap {
        _ =>
          level.closeSegments flatMap {
            _ =>
              Level(
                segmentSize = segmentSize,
                levelStorage = LevelStorage.Persistent(
                  mmapSegmentsOnWrite = level.mmapSegmentsOnWrite,
                  mmapSegmentsOnRead = level.mmapSegmentsOnRead,
                  dir = level.paths.headPath,
                  otherDirs = level.dirs.drop(1).map(dir => Dir(dir.path, 1))
                ),
                appendixStorage = AppendixStorage.Persistent(mmap = true, 4.mb),
                nextLevel = nextLevel,
                pushForward = level.pushForward,
                throttle = throttle,
                segmentConfig = level.segmentConfig,
                deleteSegmentsEventually = level.deleteSegmentsEventually,
                valuesConfig = level.valuesConfig,
                sortedIndexConfig = level.sortedIndexConfig,
                binarySearchIndexConfig = level.binarySearchIndexConfig,
                hashIndexConfig = level.hashIndexConfig,
                bloomFilterConfig = level.bloomFilterConfig
              )
          }
      }
  }

  implicit class ReopenLevelZero(level: LevelZero)(implicit keyOrder: KeyOrder[Slice[Byte]] = KeyOrder.default,
                                                   ec: ExecutionContext) {

    import swaydb.core.map.serializer.LevelZeroMapEntryWriter._

    def reopen: LevelZero =
      reopen()

    def reopen(mapSize: Long = level.maps.map.size)(implicit keyValueMemorySweeper: Option[MemorySweeper.KeyValue] = TestSweeper.memorySweeperMax,
                                                    timeOrder: TimeOrder[Slice[Byte]] = TimeOrder.long,
                                                    fileSweeper: FileSweeper.Enabled = fileSweeper): LevelZero = {
      val reopened =
        level.releaseLocks flatMap {
          _ =>
            level.closeSegments flatMap {
              _ =>
                LevelZero(
                  mapSize = mapSize,
                  enableTimer = true,
                  storage =
                    Level0Storage.Persistent(
                      mmap = true,
                      dir = level.path.getParent,
                      recovery = RecoveryMode.ReportFailure
                    ),
                  nextLevel = level.nextLevel,
                  acceleration = Accelerator.brake(),
                  throttle = level.throttle
                )
            }
        }
      reopened.runRandomIO.right.value
    }

    def putKeyValues(keyValues: Iterable[KeyValue.ReadOnly]): IO[swaydb.Error.Level, Unit] =
      if (keyValues.isEmpty)
        IO.unit
      else
        keyValues.toMapEntry match {
          case Some(value) =>
            IO {
              level
                .put(_ => value)
            }

          case None =>
            IO.unit
        }
  }

  implicit class IsExpectedInLastLevel(fromValue: FromValue) {
    def toExpectedLastLevelKeyValue(key: Slice[Byte]): Option[Memory.Fixed] =
      fromValue match {
        case _: Value.Remove =>
          None
        case Value.Put(value, deadline, time) =>
          if (deadline.forall(_.hasTimeLeft()))
            Some(Memory.Put(key, value, deadline, time))
          else
            None
        case _: Value.Update | _: Value.Function | _: Value.PendingApply =>
          None
      }
  }

  implicit class SliceApplyImplicits(applies: Slice[Value.Apply]) {
    def toMemory(key: Slice[Byte]): Slice[Memory.Fixed] =
      applies map {
        case Value.Remove(deadline, time) =>
          Memory.Remove(key, deadline, time)
        case Value.Update(value, deadline, time) =>
          Memory.Update(key, value, deadline, time)
        case function: Value.Function =>
          Memory.PendingApply(key, Slice(function))
      }
  }

  implicit class KeyValueTransientImplicits(keyValues: Iterable[Transient]) {

    def toMemory: Slice[Memory] = {
      val slice = Slice.create[Memory](keyValues.size)

      keyValues foreach {
        keyValue =>
          slice add keyValue.toMemory
      }
      slice
    }

    def toMemoryResponse: Slice[Memory] = {
      val slice = Slice.create[Memory](keyValues.size)

      keyValues foreach {
        keyValue =>
          slice add keyValue.toMemoryResponse
      }
      slice
    }
  }

  implicit class ToSlice[T: ClassTag](items: Iterable[T]) {
    def toSlice: Slice[T] = {
      val slice = Slice.create[T](items.size)
      items foreach slice.add
      slice
    }

  }

  implicit class TransientToMemory(keyValue: Transient) {
    def toMemoryResponse: Memory =
      keyValue match {
        case fixed: Transient.Fixed =>
          fixed match {
            case Transient.Remove(key, _, deadline, time, _, _, _, _, _, _) =>
              Memory.Remove(key, deadline, time)

            case Transient.Update(key, _, value, deadline, time, _, _, _, _, _, _) =>
              Memory.Update(key, value, deadline, time)

            case Transient.Put(key, _, value, deadline, time, _, _, _, _, _, _) =>
              Memory.Put(key, value, deadline, time)

            case Transient.Function(key, _, function, time, _, _, _, _, _, _) =>
              Memory.Function(key, function, time)

            case Transient.PendingApply(key, _, applies, _, _, _, _, _, _) =>
              Memory.PendingApply(key, applies)
          }

        case range: Transient.Range =>
          range match {
            case Transient.Range(fromKey, toKey, mergedKey, _, fromValue, rangeValue, _, _, _, _, _, _, _) =>
              Memory.Range(fromKey, toKey, fromValue, rangeValue)
          }
      }

    def toMemory: Memory = {
      keyValue match {
        case _ =>
          toMemoryResponse
      }
    }
  }

  implicit class TransientsToMemory(keyValues: Iterable[KeyValue]) {
    def toMemory: Slice[Memory] = {
      val slice = Slice.create[Memory](keyValues.size)

      keyValues foreach {
        case readOnly: ReadOnly =>
          slice add readOnly.toMemory

        case writeOnly: Transient =>
          slice add writeOnly.toMemory
      }

      slice
    }
  }

  implicit class ReadOnlyToMemory(keyValues: Iterable[KeyValue.ReadOnly]) {
    def toTransient: Slice[Transient] =
      toTransient()

    def toTransient(valuesConfig: ValuesBlock.Config = ValuesBlock.Config.random,
                    sortedIndexConfig: SortedIndexBlock.Config = SortedIndexBlock.Config.random,
                    binarySearchIndexConfig: BinarySearchIndexBlock.Config = BinarySearchIndexBlock.Config.random,
                    hashIndexConfig: HashIndexBlock.Config = HashIndexBlock.Config.random,
                    bloomFilterConfig: BloomFilterBlock.Config = BloomFilterBlock.Config.random)(implicit keyOrder: KeyOrder[Slice[Byte]] = KeyOrder.default,
                                                                                                 keyValueMemorySweeper: Option[MemorySweeper.KeyValue] = TestSweeper.memorySweeperMax): Slice[Transient] = {
      val slice = Slice.create[Transient](keyValues.size)

      keyValues foreach {
        keyValue =>
          slice add
            keyValue.toTransient(
              valuesConfig = valuesConfig,
              sortedIndexConfig = sortedIndexConfig,
              binarySearchIndexConfig = binarySearchIndexConfig,
              hashIndexConfig = hashIndexConfig,
              bloomFilterConfig = bloomFilterConfig,
              previous = slice.lastOption
            )
      }
      slice
    }

    def toMemory: Slice[Memory] = {
      val slice = Slice.create[Memory](keyValues.size)

      keyValues foreach {
        keyValue =>
          slice add keyValue.toMemory
      }
      slice
    }
  }

  implicit class ValuesConfig(values: ValuesBlock.Config.type) {
    def random: ValuesBlock.Config =
      random(randomBoolean())

    def random(hasCompression: Boolean): ValuesBlock.Config =
      ValuesBlock.Config(
        compressDuplicateValues = randomBoolean(),
        compressDuplicateRangeValues = randomBoolean(),
        ioStrategy = _ => randomIOAccess(),
        compressions = _ => if (hasCompression) randomCompressions() else Seq.empty
      )
  }

  implicit class SortedIndexConfig(values: SortedIndexBlock.Config.type) {
    def random: SortedIndexBlock.Config =
      random(randomBoolean())

    def random(hasCompression: Boolean): SortedIndexBlock.Config =
      SortedIndexBlock.Config(
        ioStrategy = _ => randomIOAccess(),
        prefixCompressionResetCount = randomIntMax(10),
        enableAccessPositionIndex = randomBoolean(),
        normaliseIndex = randomBoolean(),
        compressions = _ => if (hasCompression) randomCompressions() else Seq.empty
      )
  }

  implicit class BinarySearchIndexConfig(values: BinarySearchIndexBlock.Config.type) {
    def random: BinarySearchIndexBlock.Config =
      random(randomBoolean())

    def random(hasCompression: Boolean): BinarySearchIndexBlock.Config =
      BinarySearchIndexBlock.Config(
        enabled = randomBoolean(),
        format = randomBinarySearchFormat(),
        minimumNumberOfKeys = randomIntMax(5),
        searchSortedIndexDirectlyIfPossible = randomBoolean(),
        fullIndex = randomBoolean(),
        ioStrategy = _ => randomIOAccess(),
        compressions = _ => if (hasCompression) randomCompressions() else Seq.empty
      )
  }

  implicit class HashIndexConfig(values: HashIndexBlock.Config.type) {
    def random: HashIndexBlock.Config =
      random(randomBoolean())

    def random(hasCompression: Boolean): HashIndexBlock.Config =
      HashIndexBlock.Config(
        maxProbe = randomIntMax(10),
        minimumNumberOfKeys = randomIntMax(5),
        minimumNumberOfHits = randomIntMax(5),
        format = randomHashIndexSearchFormat(),
        allocateSpace = _.requiredSpace * randomIntMax(3),
        ioStrategy = _ => randomIOAccess(),
        compressions = _ => if (hasCompression) randomCompressions() else Seq.empty
      )
  }

  implicit class BloomFilterConfig(values: BloomFilterBlock.Config.type) {
    def random: BloomFilterBlock.Config =
      random(randomBoolean())

    def random(hasCompression: Boolean): BloomFilterBlock.Config =
      BloomFilterBlock.Config(
        falsePositiveRate = Random.nextDouble() min 0.5,
        minimumNumberOfKeys = randomIntMax(5),
        optimalMaxProbe = optimalMaxProbe => optimalMaxProbe,
        blockIO = _ => randomIOAccess(),
        compressions = _ => if (hasCompression) randomCompressions() else Seq.empty
      )
  }

  implicit class SegmentConfig(values: SegmentBlock.Config.type) {
    def random: SegmentBlock.Config =
      random(randomBoolean())

    def random(hasCompression: Boolean): SegmentBlock.Config =
      new SegmentBlock.Config(
        blockIO = _ => randomIOAccess(),
        compressions = _ => if (hasCompression) randomCompressions() else Seq.empty
      )
  }

  implicit class ReadOnlyKeyValueToMemory(keyValue: KeyValue.ReadOnly)(implicit keyOrder: KeyOrder[Slice[Byte]] = KeyOrder.default,
                                                                       keyValueMemorySweeper: Option[MemorySweeper.KeyValue] = TestSweeper.memorySweeperMax) {

    def toTransient: Transient =
      toTransient(None)

    def toTransient(previous: Option[Transient],
                    valuesConfig: ValuesBlock.Config = ValuesBlock.Config.random,
                    sortedIndexConfig: SortedIndexBlock.Config = SortedIndexBlock.Config.random,
                    binarySearchIndexConfig: BinarySearchIndexBlock.Config = BinarySearchIndexBlock.Config.random,
                    hashIndexConfig: HashIndexBlock.Config = HashIndexBlock.Config.random,
                    bloomFilterConfig: BloomFilterBlock.Config = BloomFilterBlock.Config.random): Transient = {
      keyValue match {
        case memory: Memory =>
          memory match {
            case fixed: Memory.Fixed =>
              fixed match {
                case Memory.Put(key, value, deadline, time) =>
                  Transient.Put(
                    key = key,
                    normaliseToSize = None,
                    value = value,
                    deadline = deadline,
                    time = time,
                    valuesConfig = previous.map(_.valuesConfig) getOrElse valuesConfig,
                    sortedIndexConfig = previous.map(_.sortedIndexConfig) getOrElse sortedIndexConfig,
                    binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig) getOrElse binarySearchIndexConfig,
                    hashIndexConfig = previous.map(_.hashIndexConfig) getOrElse hashIndexConfig,
                    bloomFilterConfig = previous.map(_.bloomFilterConfig) getOrElse bloomFilterConfig,
                    previous = previous
                  )

                case Memory.Update(key, value, deadline, time) =>
                  Transient.Update(
                    key = key,
                    normaliseToSize = None,
                    value = value,
                    deadline = deadline,
                    time = time,
                    valuesConfig = previous.map(_.valuesConfig) getOrElse valuesConfig,
                    sortedIndexConfig = previous.map(_.sortedIndexConfig) getOrElse sortedIndexConfig,
                    binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig) getOrElse binarySearchIndexConfig,
                    hashIndexConfig = previous.map(_.hashIndexConfig) getOrElse hashIndexConfig,
                    bloomFilterConfig = previous.map(_.bloomFilterConfig) getOrElse bloomFilterConfig,
                    previous = previous
                  )

                case Memory.Remove(key, deadline, time) =>
                  Transient.Remove(
                    key = key,
                    normaliseToSize = None,
                    deadline = deadline,
                    time = time,
                    valuesConfig = previous.map(_.valuesConfig) getOrElse valuesConfig,
                    sortedIndexConfig = previous.map(_.sortedIndexConfig) getOrElse sortedIndexConfig,
                    binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig) getOrElse binarySearchIndexConfig,
                    hashIndexConfig = previous.map(_.hashIndexConfig) getOrElse hashIndexConfig,
                    bloomFilterConfig = previous.map(_.bloomFilterConfig) getOrElse bloomFilterConfig,
                    previous = previous
                  )

                case Memory.Function(key, function, time) =>
                  Transient.Function(
                    key = key,
                    normaliseToSize = None,
                    function = function,
                    time = time,
                    valuesConfig = previous.map(_.valuesConfig) getOrElse valuesConfig,
                    sortedIndexConfig = previous.map(_.sortedIndexConfig) getOrElse sortedIndexConfig,
                    binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig) getOrElse binarySearchIndexConfig,
                    hashIndexConfig = previous.map(_.hashIndexConfig) getOrElse hashIndexConfig,
                    bloomFilterConfig = previous.map(_.bloomFilterConfig) getOrElse bloomFilterConfig,
                    previous = previous
                  )

                case Memory.PendingApply(key, applies) =>
                  Transient.PendingApply(
                    key = key,
                    normaliseToSize = None,
                    applies = applies,
                    valuesConfig = previous.map(_.valuesConfig) getOrElse valuesConfig,
                    sortedIndexConfig = previous.map(_.sortedIndexConfig) getOrElse sortedIndexConfig,
                    binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig) getOrElse binarySearchIndexConfig,
                    hashIndexConfig = previous.map(_.hashIndexConfig) getOrElse hashIndexConfig,
                    bloomFilterConfig = previous.map(_.bloomFilterConfig) getOrElse bloomFilterConfig,
                    previous = previous
                  )
              }
            case Memory.Range(fromKey, toKey, fromValue, rangeValue) =>
              Transient.Range[Value.FromValue, Value.RangeValue](
                fromKey = fromKey,
                toKey = toKey,
                fromValue = fromValue,
                rangeValue = rangeValue,
                valuesConfig = previous.map(_.valuesConfig) getOrElse valuesConfig,
                sortedIndexConfig = previous.map(_.sortedIndexConfig) getOrElse sortedIndexConfig,
                binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig) getOrElse binarySearchIndexConfig,
                hashIndexConfig = previous.map(_.hashIndexConfig) getOrElse hashIndexConfig,
                bloomFilterConfig = previous.map(_.bloomFilterConfig) getOrElse bloomFilterConfig,
                previous = previous
              )
          }

        case persistent: Persistent =>
          persistent match {
            case persistent: Persistent.Fixed =>
              persistent match {
                case put @ Persistent.Put(key, deadline, valueReader, time, _, _, _, _, _, _) =>
                  Transient.Put(
                    key = key,
                    normaliseToSize = None,
                    value = put.getOrFetchValue,
                    deadline = deadline,
                    time = time,
                    previous = previous,
                    valuesConfig = previous.map(_.valuesConfig) getOrElse valuesConfig,
                    sortedIndexConfig = previous.map(_.sortedIndexConfig) getOrElse sortedIndexConfig,
                    binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig) getOrElse binarySearchIndexConfig,
                    hashIndexConfig = previous.map(_.hashIndexConfig) getOrElse hashIndexConfig,
                    bloomFilterConfig = previous.map(_.bloomFilterConfig) getOrElse bloomFilterConfig
                  )

                case put @ Persistent.Update(key, deadline, valueReader, time, _, _, _, _, _, _) =>
                  Transient.Update(
                    key = key,
                    normaliseToSize = None,
                    value = put.getOrFetchValue,
                    deadline = deadline,
                    time = time,
                    previous = previous,
                    valuesConfig = previous.map(_.valuesConfig) getOrElse valuesConfig,
                    sortedIndexConfig = previous.map(_.sortedIndexConfig) getOrElse sortedIndexConfig,
                    binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig) getOrElse binarySearchIndexConfig,
                    hashIndexConfig = previous.map(_.hashIndexConfig) getOrElse hashIndexConfig,
                    bloomFilterConfig = previous.map(_.bloomFilterConfig) getOrElse bloomFilterConfig
                  )

                case function @ Persistent.Function(key, lazyFunctionReader, time, _, _, _, _, _, _) =>
                  Transient.Function(
                    key = key,
                    normaliseToSize = None,
                    function = lazyFunctionReader.value(ValuesBlock.Offset(function.valueOffset, function.valueLength)).runRandomIO.right.value,
                    time = time,
                    previous = previous,
                    valuesConfig = previous.map(_.valuesConfig) getOrElse valuesConfig,
                    sortedIndexConfig = previous.map(_.sortedIndexConfig) getOrElse sortedIndexConfig,
                    binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig) getOrElse binarySearchIndexConfig,
                    hashIndexConfig = previous.map(_.hashIndexConfig) getOrElse hashIndexConfig,
                    bloomFilterConfig = previous.map(_.bloomFilterConfig) getOrElse bloomFilterConfig
                  )

                case pendingApply: Persistent.PendingApply =>
                  Transient.PendingApply(
                    key = pendingApply.key,
                    normaliseToSize = None,
                    applies = pendingApply.getOrFetchApplies,
                    previous = previous,
                    valuesConfig = previous.map(_.valuesConfig) getOrElse valuesConfig,
                    sortedIndexConfig = previous.map(_.sortedIndexConfig) getOrElse sortedIndexConfig,
                    binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig) getOrElse binarySearchIndexConfig,
                    hashIndexConfig = previous.map(_.hashIndexConfig) getOrElse hashIndexConfig,
                    bloomFilterConfig = previous.map(_.bloomFilterConfig) getOrElse bloomFilterConfig
                  )

                case Persistent.Remove(_key, deadline, time, _, _, _, _) =>
                  Transient.Remove(
                    key = _key,
                    normaliseToSize = None,
                    deadline = deadline,
                    time = time,
                    previous = previous,
                    valuesConfig = previous.map(_.valuesConfig) getOrElse valuesConfig,
                    sortedIndexConfig = previous.map(_.sortedIndexConfig) getOrElse sortedIndexConfig,
                    binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig) getOrElse binarySearchIndexConfig,
                    hashIndexConfig = previous.map(_.hashIndexConfig) getOrElse hashIndexConfig,
                    bloomFilterConfig = previous.map(_.bloomFilterConfig) getOrElse bloomFilterConfig
                  )
              }

            case range @ Persistent.Range(_fromKey, _toKey, _, _, _, _, _, _, _) =>
              val (fromValue, rangeValue) = range.fetchFromAndRangeValueUnsafe.runRandomIO.right.value
              Transient.Range(
                fromKey = _fromKey,
                toKey = _toKey,
                fromValue = fromValue,
                rangeValue = rangeValue,
                valuesConfig = previous.map(_.valuesConfig) getOrElse valuesConfig,
                sortedIndexConfig = previous.map(_.sortedIndexConfig) getOrElse sortedIndexConfig,
                binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig) getOrElse binarySearchIndexConfig,
                hashIndexConfig = previous.map(_.hashIndexConfig) getOrElse hashIndexConfig,
                bloomFilterConfig = previous.map(_.bloomFilterConfig) getOrElse bloomFilterConfig,
                previous = previous
              )
          }
      }
    }

    def toMemory: Memory = {
      keyValue match {
        case memory: Memory =>
          memory

        case persistent: Persistent =>
          persistent.toMemoryResponse
      }
    }

    def toMemoryResponse: Memory = {
      keyValue match {
        case memory: Memory =>
          memory

        case persistent: Persistent =>
          persistent match {
            case persistent: Persistent.Fixed =>
              persistent match {
                case put @ Persistent.Put(key, deadline, valueReader, time, nextIndexOffset, nextIndexSize, indexOffset, valueOffset, valueLength, _) =>
                  Memory.Put(key, put.getOrFetchValue, deadline, time)

                case update @ Persistent.Update(key, deadline, valueReader, time, nextIndexOffset, nextIndexSize, indexOffset, valueOffset, valueLength, _) =>
                  Memory.Update(key, update.getOrFetchValue, deadline, time)

                case function @ Persistent.Function(key, lazyFunctionReader, time, nextIndexOffset, nextIndexSize, indexOffset, valueOffset, valueLength, _) =>
                  Memory.Function(key, function.getOrFetchFunction.runRandomIO.right.value, time)

                case pendingApply @ Persistent.PendingApply(key, time, deadline, lazyPendingApplyValueReader, nextIndexOffset, nextIndexSize, indexOffset, valueOffset, valueLength, _) =>
                  Memory.PendingApply(key, pendingApply.getOrFetchApplies)

                case Persistent.Remove(_key, deadline, time, indexOffset, nextIndexOffset, nextIndexSize, _) =>
                  Memory.Remove(_key, deadline, time)
              }

            case range @ Persistent.Range(_fromKey, _toKey, _, nextIndexOffset, nextIndexSize, indexOffset, valueOffset, valueLength, _) =>
              val (fromValue, rangeValue) = range.fetchFromAndRangeValueUnsafe.runRandomIO.right.value
              Memory.Range(_fromKey, _toKey, fromValue, rangeValue)
          }
      }
    }
  }

  def randomStringOption: Option[Slice[Byte]] =
    if (randomBoolean())
      Some(randomString)
    else
      None

  def randomString =
    randomCharacters()

  def randomDeadlineOption: Option[Deadline] =
    randomDeadlineOption()

  def randomDeadlineOption(expired: Boolean = randomBoolean()): Option[Deadline] =
    if (randomBoolean())
      Some(randomDeadline(expired))
    else
      None

  def randomDeadline(expired: Boolean = randomBoolean()): Deadline =
    if (expired && randomBoolean())
      0.seconds.fromNow - (randomIntMax(30) + 10).seconds
    else
      (randomIntMax(60) max 30).seconds.fromNow

  def randomDeadUpdateOrExpiredPut(key: Slice[Byte]): Memory.Fixed =
    eitherOne(
      randomFixedKeyValue(key, includePuts = false),
      randomPutKeyValue(key, deadline = Some(expiredDeadline())),
    )

  def randomPutKeyValue(key: Slice[Byte],
                        value: Option[Slice[Byte]] = randomStringOption,
                        deadline: Option[Deadline] = randomDeadlineOption)(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Put = {
    val put = Memory.Put(key, value, deadline, testTimer.next)
    //println(put)
    put
  }

  def randomExpiredPutKeyValue(key: Slice[Byte],
                               value: Option[Slice[Byte]] = randomStringOption)(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Put =
    randomPutKeyValue(key, value, deadline = Some(expiredDeadline()))

  def randomUpdateKeyValue(key: Slice[Byte],
                           value: Option[Slice[Byte]] = randomStringOption,
                           deadline: Option[Deadline] = randomDeadlineOption)(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Update =
    Memory.Update(key, value, deadline, testTimer.next)

  def randomRemoveKeyValue(key: Slice[Byte],
                           deadline: Option[Deadline] = randomDeadlineOption)(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Remove =
    Memory.Remove(key, deadline, testTimer.next)

  def randomRemoveAny(from: Slice[Byte],
                      to: Slice[Byte],
                      addFunctions: Boolean = true)(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory =
    eitherOne(
      left = randomRemoveOrUpdateOrFunctionRemove(from, addFunctions),
      right = randomRemoveRange(from, to)
    )

  def randomRemoveOrUpdateOrFunctionRemoveValue(addFunctions: Boolean = true)(implicit testTimer: TestTimer = TestTimer.Incremental()): RangeValue = {
    val value = randomRemoveOrUpdateOrFunctionRemove(Slice.emptyBytes, addFunctions).toRangeValue().runRandomIO.right.value
    //println(value)
    value
  }

  def randomRemoveFunctionValue()(implicit testTimer: TestTimer = TestTimer.Incremental()): Value.Function =
    randomFunctionKeyValue(Slice.emptyBytes, SwayFunctionOutput.Remove).toRangeValue().runRandomIO.right.value

  def randomFunctionValue(output: SwayFunctionOutput = randomFunctionOutput())(implicit testTimer: TestTimer = TestTimer.Incremental()): Value.Function =
    randomFunctionKeyValue(Slice.emptyBytes, SwayFunctionOutput.Remove).toRangeValue().runRandomIO.right.value

  def randomRemoveOrUpdateOrFunctionRemoveValueOption(addFunctions: Boolean = true)(implicit testTimer: TestTimer = TestTimer.Incremental()): Option[RangeValue] =
    eitherOne(
      left = None,
      right = Some(randomRemoveOrUpdateOrFunctionRemoveValue(addFunctions))
    )

  /**
   * Removes can occur by [[Memory.Remove]], [[Memory.Update]] with expiry or [[Memory.Function]] with remove output.
   */
  def randomRemoveOrUpdateOrFunctionRemove(key: Slice[Byte],
                                           addFunctions: Boolean = true)(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Fixed =
    if (randomBoolean())
      randomRemoveKeyValue(key, randomExpiredDeadlineOption())
    else if (randomBoolean && addFunctions)
      randomFunctionKeyValue(key, randomRemoveFunctionOutput())
    else
      randomUpdateKeyValue(key, randomStringOption, Some(expiredDeadline()))

  def randomRemoveFunctionOutput() =
    eitherOne(
      SwayFunctionOutput.Remove,
      SwayFunctionOutput.Expire(expiredDeadline()),
      SwayFunctionOutput.Update(randomStringOption, Some(expiredDeadline()))
    )

  def randomUpdateFunctionOutput() =
    eitherOne(
      SwayFunctionOutput.Expire(randomDeadline(false)),
      SwayFunctionOutput.Update(randomStringOption, randomDeadlineOption(false))
    )

  def randomRemoveRange(from: Slice[Byte],
                        to: Slice[Byte],
                        addFunctions: Boolean = true)(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Range =
    randomRangeKeyValue(
      from = from,
      to = to,
      fromValue = randomRemoveOrUpdateOrFunctionRemoveValueOption(addFunctions),
      rangeValue = randomRemoveOrUpdateOrFunctionRemoveValue(addFunctions)
    )

  /**
   * Creates remove ranges of random range slices slice for all input key-values.
   */
  def randomRemoveRanges(keyValues: Iterable[Memory])(implicit testTimer: TestTimer = TestTimer.Incremental()): Iterator[Memory.Range] =
    keyValues
      .grouped(randomIntMax(100) max 1)
      .flatMap {
        groupKeyValues =>
          if (groupKeyValues.isEmpty)
            None
          else {
            val maxKeyInt = getMaxKey(groupKeyValues.last.toTransient).maxKey.readInt()
            assert(groupKeyValues.head.key.readInt() < maxKeyInt + 1)
            Some(
              randomRemoveRange(
                from = groupKeyValues.head.key,
                to = maxKeyInt + 1
              )
            )
          }
      }

  def randomPendingApplyKeyValue(key: Slice[Byte],
                                 max: Int = 5,
                                 value: Option[Slice[Byte]] = randomStringOption,
                                 deadline: Option[Deadline] = randomDeadlineOption,
                                 functionOutput: SwayFunctionOutput = randomFunctionOutput(),
                                 includeFunctions: Boolean = true)(implicit testTimer: TestTimer = TestTimer.Incremental()) =
    Memory.PendingApply(
      key = key,
      applies =
        randomApplies(
          max = max,
          value = value,
          deadline = deadline,
          functionOutput = functionOutput,
          includeFunctions = includeFunctions
        )
    )

  def createFunction(key: Slice[Byte],
                     swayFunction: SwayFunction)(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Function = {
    val functionId = Slice.writeInt(functionIdGenerator.incrementAndGet())
    functionStore.put(functionId, swayFunction)
    Memory.Function(key, functionId, testTimer.next)
  }

  def randomFunctionKeyValue(key: Slice[Byte],
                             output: SwayFunctionOutput = randomFunctionOutput())(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Function =
    createFunction(
      key = key,
      swayFunction = randomSwayFunction(output)
    )

  def randomFunctionNoDeadlineKeyValue(key: Slice[Byte],
                                       output: SwayFunctionOutput = randomFunctionOutput())(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Function =
    createFunction(
      key = key,
      swayFunction = randomSwayFunctionNoDeadline(output)
    )

  def randomKeyFunctionKeyValue(key: Slice[Byte],
                                output: SwayFunctionOutput = randomFunctionOutput())(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Function =
    createFunction(
      key = key,
      swayFunction = SwayFunction.Key(_ => output)
    )

  def randomKeyDeadlineFunctionKeyValue(key: Slice[Byte],
                                        output: SwayFunctionOutput = randomFunctionOutput())(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Function =
    createFunction(
      key = key,
      swayFunction = SwayFunction.KeyDeadline((_, _) => output)
    )

  def randomKeyValueFunctionKeyValue(key: Slice[Byte],
                                     output: SwayFunctionOutput = randomFunctionOutput())(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Function =
    createFunction(
      key = key,
      swayFunction = SwayFunction.KeyValue((_, _) => output)
    )

  def randomKeyValueDeadlineFunctionKeyValue(key: Slice[Byte],
                                             output: SwayFunctionOutput = randomFunctionOutput())(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Function =
    createFunction(
      key = key,
      swayFunction = SwayFunction.KeyValueDeadline((_, _, _) => output)
    )

  def randomValueFunctionKeyValue(key: Slice[Byte],
                                  output: SwayFunctionOutput = randomFunctionOutput())(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Function =
    createFunction(
      key = key,
      swayFunction = SwayFunction.Value(_ => output)
    )

  def randomValueDeadlineFunctionKeyValue(key: Slice[Byte],
                                          output: SwayFunctionOutput = randomFunctionOutput())(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Function =
    createFunction(
      key = key,
      swayFunction = SwayFunction.ValueDeadline((_, _) => output)
    )

  def randomFunctionOutput(addRemoves: Boolean = randomBoolean(), expiredDeadline: Boolean = randomBoolean()): SwayFunctionOutput =
    if (addRemoves && randomBoolean())
      SwayFunctionOutput.Remove
    else if (randomBoolean())
      SwayFunctionOutput.Nothing
    else
      randomFunctionUpdateOutput(expiredDeadline)

  def randomFunctionUpdateOutput(expiredDeadline: Boolean = randomBoolean()): SwayFunctionOutput =
    if (randomBoolean())
      SwayFunctionOutput.Expire(randomDeadline(expiredDeadline))
    else
      SwayFunctionOutput.Update(randomStringOption, randomDeadlineOption(expiredDeadline))

  def randomRequiresKeyFunction(functionOutput: SwayFunctionOutput = randomFunctionOutput()): SwayFunction.RequiresKey =
    Random.shuffle(
      Seq[SwayFunction.RequiresKey](
        SwayFunction.Key(_ => functionOutput),
        SwayFunction.KeyValue((_, _) => functionOutput),
        SwayFunction.KeyDeadline((_, _) => functionOutput),
        SwayFunction.KeyValueDeadline((_, _, _) => functionOutput)
      )
    ).head

  def randomRequiresKeyOnlyWithOptionDeadlineFunction(functionOutput: SwayFunctionOutput = randomFunctionOutput()): SwayFunction.RequiresKey =
    Random.shuffle(
      Seq[SwayFunction.RequiresKey](
        SwayFunction.Key(_ => functionOutput),
        SwayFunction.KeyDeadline((_, _) => functionOutput)
      )
    ).head

  def randomValueOnlyFunction(functionOutput: SwayFunctionOutput = randomFunctionOutput()): SwayFunction.RequiresValue =
    Random.shuffle(
      Seq[SwayFunction.RequiresValue](
        SwayFunction.Value(_ => functionOutput),
        SwayFunction.ValueDeadline((_, _) => functionOutput),
      )
    ).head

  def randomRequiresValueWithOptionalKeyAndDeadlineFunction(functionOutput: SwayFunctionOutput = randomFunctionOutput()): SwayFunction.RequiresValue =
    Random.shuffle(
      Seq[SwayFunction.RequiresValue](
        SwayFunction.Value(_ => functionOutput),
        SwayFunction.KeyValueDeadline((_, _, _) => functionOutput),
        SwayFunction.KeyValue((_, _) => functionOutput),
        SwayFunction.ValueDeadline((_, _) => functionOutput)
      )
    ).head

  def randomSwayFunctionNoDeadline(functionOutput: SwayFunctionOutput = randomFunctionOutput()): SwayFunction =
    Random.shuffle(
      Seq(
        SwayFunction.Value(_ => functionOutput),
        SwayFunction.Key(_ => functionOutput),
        SwayFunction.KeyValue((_, _) => functionOutput)
      )
    ).head

  def randomRequiresDeadlineFunction(functionOutput: SwayFunctionOutput = randomFunctionOutput()): SwayFunction.RequiresDeadline =
    Random.shuffle(
      Seq[SwayFunction.RequiresDeadline](
        SwayFunction.KeyDeadline((_, _) => functionOutput),
        SwayFunction.KeyValueDeadline((_, _, _) => functionOutput),
        SwayFunction.ValueDeadline((_, _) => functionOutput)
      )
    ).head

  implicit class FunctionOutputImplicits(functionOutput: SwayFunctionOutput) {
    def toMemory(key: Slice[Byte],
                 time: Time): Memory.Fixed = {
      val outputFixed =
        functionOutput match {
          case SwayFunctionOutput.Remove =>
            Memory.Remove(key, None, time)

          case SwayFunctionOutput.Expire(deadline) =>
            Memory.Remove(key, Some(deadline), time)

          case SwayFunctionOutput.Update(newValue, newDeadline) =>
            Memory.Update(key, newValue, newDeadline, time)
        }
      //println(s"outputFixed: $outputFixed")
      outputFixed
    }
  }

  def randomSwayFunction(functionOutput: SwayFunctionOutput = randomFunctionOutput()): SwayFunction =
    if (randomBoolean())
      randomRequiresKeyFunction(functionOutput)
    else
      randomValueOnlyFunction(functionOutput)

  def randomFunctionId(functionOutput: SwayFunctionOutput = randomFunctionOutput()): Slice[Byte] = {
    val functionId = Slice.writeInt(functionIdGenerator.incrementAndGet())
    functionStore.put(functionId, randomSwayFunction(functionOutput))
    functionId
  }

  def randomApply(value: Option[Slice[Byte]] = randomStringOption,
                  deadline: Option[Deadline] = randomDeadlineOption,
                  addRemoves: Boolean = randomBoolean(),
                  functionOutput: SwayFunctionOutput = randomFunctionOutput(),
                  includeFunctions: Boolean = true)(implicit testTimer: TestTimer = TestTimer.Incremental()) =
    if (addRemoves && randomBoolean())
      Value.Remove(deadline, testTimer.next)
    else if (includeFunctions && randomBoolean())
      Value.Function(randomFunctionId(functionOutput), testTimer.next)
    else
      Value.Update(value, deadline, testTimer.next)

  def randomApplyWithDeadline(value: Option[Slice[Byte]] = randomStringOption,
                              addRangeRemoves: Boolean = randomBoolean(),
                              deadline: Deadline = randomDeadline())(implicit testTimer: TestTimer = TestTimer.Incremental()) =
    if (addRangeRemoves && randomBoolean())
      Value.Remove(Some(deadline), testTimer.next)
    else
      Value.Update(value, Some(deadline), testTimer.next)

  def randomApplies(max: Int = 5,
                    value: Option[Slice[Byte]] = randomStringOption,
                    deadline: Option[Deadline] = randomDeadlineOption,
                    addRemoves: Boolean = randomBoolean(),
                    functionOutput: SwayFunctionOutput = randomFunctionOutput(),
                    includeFunctions: Boolean = true)(implicit testTimer: TestTimer = TestTimer.Incremental()): Slice[Value.Apply] =
    Slice {
      (1 to (Random.nextInt(max) max 1)).map {
        _ =>
          randomApply(
            value = value,
            deadline = deadline,
            addRemoves = addRemoves,
            functionOutput = functionOutput,
            includeFunctions = includeFunctions
          )
      } toArray
    }

  def randomAppliesWithDeadline(max: Int = 5,
                                value: Option[Slice[Byte]] = randomStringOption,
                                addRangeRemoves: Boolean = randomBoolean(),
                                deadline: Deadline = randomDeadline())(implicit testTimer: TestTimer = TestTimer.Incremental()): Slice[Value.Apply] =
    Slice {
      (1 to (Random.nextInt(max) max 1)).map {
        _ =>
          randomApplyWithDeadline(
            value = value,
            addRangeRemoves = addRangeRemoves,
            deadline = deadline
          )
      } toArray
    }

  def randomTransientKeyValue(key: Slice[Byte],
                              toKey: Option[Slice[Byte]],
                              value: Option[Slice[Byte]] = randomStringOption,
                              fromValue: Option[FromValue] = randomFromValueOption(),
                              rangeValue: RangeValue = randomRangeValue(),
                              deadline: Option[Deadline] = randomDeadlineOption,
                              time: Time = Time.empty,
                              previous: Option[Transient] = None,
                              valuesConfig: ValuesBlock.Config = ValuesBlock.Config.random,
                              sortedIndexConfig: SortedIndexBlock.Config = SortedIndexBlock.Config.random,
                              binarySearchIndexConfig: BinarySearchIndexBlock.Config = BinarySearchIndexBlock.Config.random,
                              hashIndexConfig: HashIndexBlock.Config = HashIndexBlock.Config.random,
                              bloomFilterConfig: BloomFilterBlock.Config = BloomFilterBlock.Config.random,
                              functionOutput: SwayFunctionOutput = randomFunctionOutput(),
                              includePendingApply: Boolean = true,
                              includeFunctions: Boolean = true,
                              includeRemoves: Boolean = true,
                              includePuts: Boolean = true,
                              includeRanges: Boolean = true): Transient =
    if (toKey.isDefined && includeRanges && randomBoolean())
      Transient.Range(
        fromKey = key,
        toKey = toKey.get,
        fromValue = fromValue,
        rangeValue = rangeValue,
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig,
        previous = previous
      )
    else
      randomFixedTransientKeyValue(
        key = key,
        value = value,
        deadline = deadline,
        time = time,
        previous = previous,
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig,
        functionOutput = functionOutput,
        includePendingApply = includePendingApply,
        includeFunctions = includeFunctions,
        includeRemoves = includeRemoves,
        includePuts = includePuts
      )

  def randomFixedTransientKeyValue(key: Slice[Byte],
                                   value: Option[Slice[Byte]] = randomStringOption,
                                   deadline: Option[Deadline] = randomDeadlineOption,
                                   time: Time = Time.empty,
                                   previous: Option[Transient] = None,
                                   valuesConfig: ValuesBlock.Config = ValuesBlock.Config.random,
                                   sortedIndexConfig: SortedIndexBlock.Config = SortedIndexBlock.Config.random,
                                   binarySearchIndexConfig: BinarySearchIndexBlock.Config = BinarySearchIndexBlock.Config.random,
                                   hashIndexConfig: HashIndexBlock.Config = HashIndexBlock.Config.random,
                                   bloomFilterConfig: BloomFilterBlock.Config = BloomFilterBlock.Config.random,
                                   functionOutput: SwayFunctionOutput = randomFunctionOutput(),
                                   includePendingApply: Boolean = true,
                                   includeFunctions: Boolean = true,
                                   includeRemoves: Boolean = true,
                                   includePuts: Boolean = true): Transient.Fixed =
    if (includePuts && randomBoolean())
      Transient.Put(
        key = key,
        normaliseToSize = None,
        value = value,
        deadline = deadline,
        time = time,
        previous = previous,
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig
      )
    //    else if (includeRemoves && randomBoolean())
    //      Transient.Remove(
    //        key = key,
    //        deadline = deadline,
    //        time = time,
    //        previous = previous,
    //        valuesConfig = valuesConfig,
    //        sortedIndexConfig = sortedIndexConfig,
    //        binarySearchIndexConfig = binarySearchIndexConfig,
    //        hashIndexConfig = hashIndexConfig,
    //        bloomFilterConfig = bloomFilterConfig
    //      )
    else if (includeFunctions && randomBoolean())
      Transient.Function(
        key = key,
        normaliseToSize = None,
        function = randomFunctionId(functionOutput),
        time = time,
        previous = previous,
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig
      )
    else if (includePendingApply && randomBoolean())
      Transient.PendingApply(
        key = key,
        normaliseToSize = None,
        applies =
          randomApplies(
            max = 10,
            value = value,
            deadline = deadline,
            addRemoves = includeRemoves,
            functionOutput = functionOutput,
            includeFunctions = includeFunctions
          ),
        previous = previous,
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig
      )
    else
      Transient.Update(
        key = key,
        normaliseToSize = None,
        value = value,
        deadline = deadline,
        time = time,
        previous = previous,
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig
      )

  def randomFixedKeyValue(key: Slice[Byte],
                          value: Option[Slice[Byte]] = randomStringOption,
                          deadline: Option[Deadline] = randomDeadlineOption,
                          functionOutput: SwayFunctionOutput = randomFunctionOutput(),
                          includePendingApply: Boolean = true,
                          includeFunctions: Boolean = true,
                          includeRemoves: Boolean = true,
                          includePuts: Boolean = true)(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Fixed =
    if (includePuts && randomBoolean())
      Memory.Put(key, value, deadline, testTimer.next)
    else if (includeRemoves && randomBoolean())
      Memory.Remove(key, deadline, testTimer.next)
    else if (includeFunctions && randomBoolean())
      Memory.Function(key, randomFunctionId(functionOutput), testTimer.next)
    else if (includePendingApply && randomBoolean())
      Memory.PendingApply(
        key = key,
        applies =
          randomApplies(
            max = 10,
            value = value,
            deadline = deadline,
            addRemoves = includeRemoves,
            functionOutput = functionOutput,
            includeFunctions = includeFunctions
          )
      )
    else
      Memory.Update(key, value, deadline, testTimer.next)

  def randomCompression(minCompressionPercentage: Double = Double.MinValue): CompressionInternal =
    CompressionInternal.random(minCompressionPercentage = minCompressionPercentage)

  def randomCompressionLZ4OrSnappy(minCompressionPercentage: Double = Double.MinValue): CompressionInternal =
    CompressionInternal.randomLZ4OrSnappy(minCompressionPercentage = minCompressionPercentage)

  def randomCompressionSnappy(minCompressionPercentage: Double = Double.MinValue): CompressionInternal =
    CompressionInternal.randomSnappy(minCompressionPercentage = minCompressionPercentage)

  def randomCompressionLZ4(minCompressionPercentage: Double = Double.MinValue): CompressionInternal =
    CompressionInternal.randomLZ4(minCompressionPercentage = minCompressionPercentage)

  def randomCompressions(minCompressionPercentage: Double = Double.MinValue): Seq[CompressionInternal] =
    (0 to randomIntMax(3) + 1) map (_ => randomCompression(minCompressionPercentage))

  def randomCompressionsOrEmpty(minCompressionPercentage: Double = Double.MinValue): Seq[CompressionInternal] =
    eitherOne(
      Seq.empty,
      randomCompressions(minCompressionPercentage)
    )

  def randomCompressionsLZ4OrSnappy(minCompressionPercentage: Double = Double.MinValue): Seq[CompressionInternal] =
    (0 to randomIntMax(3) + 1) map (_ => randomCompressionLZ4OrSnappy(minCompressionPercentage))

  def randomCompressionsLZ4OrSnappyOrEmpty(minCompressionPercentage: Double = Double.MinValue): Seq[CompressionInternal] =
    eitherOne(
      Seq.empty,
      randomCompressionsLZ4OrSnappy(minCompressionPercentage)
    )

  def randomRangeKeyValue(from: Slice[Byte],
                          to: Slice[Byte],
                          fromValue: Option[FromValue] = randomFromValueOption()(TestTimer.random),
                          rangeValue: RangeValue = randomRangeValue()(TestTimer.random)): Memory.Range = {
    val range = Memory.Range(from, to, fromValue, rangeValue)
    //println(range)
    range
  }

  def randomRangeKeyValueWithDeadline(from: Slice[Byte],
                                      to: Slice[Byte],
                                      fromValue: Option[FromValue] = randomFromValueWithDeadlineOption()(TestTimer.random),
                                      rangeValue: RangeValue = randomRangeValueWithDeadline()(TestTimer.random)): Memory.Range = {
    val range = Memory.Range(from, to, fromValue, rangeValue)
    //println(range)
    range
  }

  def randomRangeKeyValueWithFromValueExpiredDeadline(from: Slice[Byte],
                                                      to: Slice[Byte],
                                                      fromValue: Option[FromValue] = randomFromValueWithDeadlineOption(deadline = expiredDeadline())(TestTimer.random),
                                                      rangeValue: RangeValue = randomRangeValueWithDeadline()(TestTimer.random)): Memory.Range =
    randomRangeKeyValueWithDeadline(from, to, fromValue, rangeValue)

  def randomRangeKeyValueForDeadline(from: Slice[Byte],
                                     to: Slice[Byte],
                                     deadline: Deadline = randomDeadline()): Memory.Range =
    Memory.Range(
      fromKey = from,
      toKey = to,
      fromValue = randomFromValueWithDeadlineOption(deadline = deadline)(TestTimer.random),
      rangeValue = randomRangeValueWithDeadline(deadline = deadline)(TestTimer.random)
    )

  def randomRangeValueOption(from: Slice[Byte], to: Slice[Byte]): Option[Memory.Range] =
    if (randomBoolean())
      Some(randomRangeKeyValue(from, to))
    else
      None

  def randomFromValueOption(value: Option[Slice[Byte]] = randomStringOption,
                            deadline: Option[Deadline] = randomDeadlineOption,
                            functionOutput: SwayFunctionOutput = randomFunctionOutput(),
                            addRemoves: Boolean = randomBoolean(),
                            addPut: Boolean = randomBoolean())(implicit testTimer: TestTimer = TestTimer.Incremental()): Option[Value.FromValue] =
    if (randomBoolean())
      Some(
        randomFromValue(
          value = value,
          addRemoves = addRemoves,
          functionOutput = functionOutput,
          deadline = deadline,
          addPut = addPut
        )
      )
    else
      None

  def randomFromValueWithDeadlineOption(value: Option[Slice[Byte]] = randomStringOption,
                                        addRangeRemoves: Boolean = randomBoolean(),
                                        deadline: Deadline = randomDeadline())(implicit testTimer: TestTimer = TestTimer.Incremental()): Option[Value.FromValue] =
    if (randomBoolean())
      Some(randomFromValueWithDeadline(value, addRangeRemoves, deadline))
    else
      None

  def randomUpdateRangeValue(value: Option[Slice[Byte]] = randomStringOption,
                             addRemoves: Boolean = randomBoolean(),
                             functionOutput: SwayFunctionOutput = randomUpdateFunctionOutput())(implicit testTimer: TestTimer = TestTimer.Incremental()) = {
    val deadline =
    //if removes are allowed make sure to set the deadline
      if (addRemoves)
        Some(randomDeadline(false))
      else
        randomDeadlineOption(false)

    randomRangeValue(value = value, addRemoves = addRemoves, functionOutput = functionOutput, deadline = deadline)
  }

  def randomFromValue(value: Option[Slice[Byte]] = randomStringOption,
                      addRemoves: Boolean = randomBoolean(),
                      deadline: Option[Deadline] = randomDeadlineOption,
                      functionOutput: SwayFunctionOutput = randomFunctionOutput(),
                      addPut: Boolean = randomBoolean())(implicit testTimer: TestTimer = TestTimer.Incremental()): Value.FromValue =
    if (addPut && randomBoolean())
      Value.Put(value, deadline, testTimer.next)
    else
      randomRangeValue(value = value, addRemoves = addRemoves, functionOutput = functionOutput, deadline = deadline)

  def randomRangeValue(value: Option[Slice[Byte]] = randomStringOption,
                       deadline: Option[Deadline] = randomDeadlineOption,
                       functionOutput: SwayFunctionOutput = randomFunctionOutput(),
                       addRemoves: Boolean = randomBoolean())(implicit testTimer: TestTimer = TestTimer.Incremental()): Value.RangeValue =
    if (addRemoves && randomBoolean())
      Value.Remove(deadline, testTimer.next)
    else if (randomBoolean())
      Value.Function(randomFunctionId(functionOutput), testTimer.next)
    else if (randomBoolean())
      Value.PendingApply(randomApplies(value = value, addRemoves = addRemoves, deadline = deadline, functionOutput = functionOutput))
    else
      Value.Update(value, deadline, testTimer.next)

  def randomFromValueWithDeadline(value: Option[Slice[Byte]] = randomStringOption,
                                  addRangeRemoves: Boolean = randomBoolean(),
                                  deadline: Deadline = randomDeadline())(implicit testTimer: TestTimer = TestTimer.Incremental()): Value.FromValue =
    if (randomBoolean())
      Value.Put(value, Some(deadline), testTimer.next)
    else
      randomRangeValueWithDeadline(value = value, addRangeRemoves = addRangeRemoves, deadline = deadline)

  def randomRangeValueWithDeadline(value: Option[Slice[Byte]] = randomStringOption,
                                   addRangeRemoves: Boolean = randomBoolean(),
                                   deadline: Deadline = randomDeadline())(implicit testTimer: TestTimer = TestTimer.Incremental()): Value.RangeValue =
    if (addRangeRemoves && randomBoolean())
      Value.Remove(Some(deadline), testTimer.next)
    else if (randomBoolean())
      Value.PendingApply(randomAppliesWithDeadline(value = value, deadline = deadline))
    else
      Value.Update(value, Some(deadline), testTimer.next)

  def randomCharacters(size: Int = 10) = Random.alphanumeric.take(size max 1).mkString

  def randomBytes(size: Int = 10) = Array.fill(size)(randomByte())

  def randomByteChunks(size: Int = 10, sizePerChunk: Int = 10): Slice[Slice[Byte]] = {
    val slice = Slice.create[Slice[Byte]](size)
    (1 to size) foreach {
      _ =>
        slice add Slice(randomBytes(sizePerChunk))
    }
    slice
  }

  def randomBytesSlice(size: Int = 10) = Slice(randomBytes(size))

  def randomBytesSliceOption(size: Int = 10): Option[Slice[Byte]] =
    if (randomBoolean() || size == 0)
      None
    else
      Some(randomBytesSlice(size))

  def someByteSlice(size: Int = 10): Option[Slice[Byte]] =
    if (size == 0)
      None
    else
      Some(randomBytesSlice(size))

  def randomByte() = (Random.nextInt(256) - 128).toByte

  def ints(numbers: Int): Int =
    (1 to numbers).foldLeft("") {
      case (concat, _) =>
        concat + Math.abs(Random.nextInt(9)).toString
    }.toInt

  def randomInt(minus: Int = 0) = Math.abs(Random.nextInt(Int.MaxValue)) - minus - 1

  def randomIntMax(max: Int = Int.MaxValue) =
    Math.abs(Random.nextInt(max))

  def randomIntMin(min: Int) =
    Math.abs(randomIntMax()) max min

  def randomIntMaxOption(max: Int = Int.MaxValue) =
    if (randomBoolean())
      Some(randomIntMax(max))
    else
      None

  def randomIntKeyStringValues(count: Int = 5,
                               startId: Option[Int] = None,
                               valueSize: Int = 50,
                               addRemoves: Boolean = false,
                               addRanges: Boolean = false,
                               addRemoveDeadlines: Boolean = false,
                               addPutDeadlines: Boolean = false)(implicit testTimer: TestTimer = TestTimer.Incremental(),
                                                                 keyOrder: KeyOrder[Slice[Byte]] = KeyOrder.default,
                                                                 keyValueMemorySweeper: Option[MemorySweeper.KeyValue] = TestSweeper.memorySweeperMax): Slice[Transient] =
    randomKeyValues(
      count = count,
      startId = startId,
      valueSize = valueSize,
      addRemoves = addRemoves,
      addRanges = addRanges,
      addRemoveDeadlines = addRemoveDeadlines,
      addPutDeadlines = addPutDeadlines
    )

  def randomizedKeyValues(count: Int = 5,
                          startId: Option[Int] = None,
                          valueSize: Int = 50,
                          addPut: Boolean = true,
                          addRemoves: Boolean = randomBoolean(),
                          addRangeRemoves: Boolean = randomBoolean(),
                          addUpdates: Boolean = randomBoolean(),
                          addFunctions: Boolean = randomBoolean(),
                          addRanges: Boolean = randomBoolean(),
                          addPendingApply: Boolean = randomBoolean(),
                          addRemoveDeadlines: Boolean = randomBoolean(),
                          addPutDeadlines: Boolean = randomBoolean(),
                          addExpiredPutDeadlines: Boolean = randomBoolean(),
                          addUpdateDeadlines: Boolean = randomBoolean(),
                          valuesConfig: ValuesBlock.Config = ValuesBlock.Config.random,
                          sortedIndexConfig: SortedIndexBlock.Config = SortedIndexBlock.Config.random,
                          binarySearchIndexConfig: BinarySearchIndexBlock.Config = BinarySearchIndexBlock.Config.random,
                          hashIndexConfig: HashIndexBlock.Config = HashIndexBlock.Config.random,
                          bloomFilterConfig: BloomFilterBlock.Config = BloomFilterBlock.Config.random)(implicit testTimer: TestTimer = TestTimer.Incremental(),
                                                                                                       keyOrder: KeyOrder[Slice[Byte]] = KeyOrder.default,
                                                                                                       keyValueMemorySweeper: Option[MemorySweeper.KeyValue] = TestSweeper.memorySweeperMax): Slice[Transient] =
    randomKeyValues(
      count = count,
      startId = startId,
      valueSize = valueSize,
      addPut = addPut,
      addRemoves = addRemoves,
      addRangeRemoves = addRangeRemoves,
      addUpdates = addUpdates,
      addFunctions = addFunctions,
      addRanges = addRanges,
      addPendingApply = addPendingApply,
      addRemoveDeadlines = addRemoveDeadlines,
      addPutDeadlines = addPutDeadlines,
      addExpiredPutDeadlines = addExpiredPutDeadlines,
      addUpdateDeadlines = addUpdateDeadlines,
      valuesConfig = valuesConfig,
      sortedIndexConfig = sortedIndexConfig,
      binarySearchIndexConfig = binarySearchIndexConfig,
      hashIndexConfig = hashIndexConfig,
      bloomFilterConfig = bloomFilterConfig
    )

  def randomPutKeyValues(count: Int = 5,
                         startId: Option[Int] = None,
                         valueSize: Int = 50,
                         addRemoves: Boolean = false,
                         addRanges: Boolean = false,
                         addRemoveDeadlines: Boolean = false,
                         addPutDeadlines: Boolean = true,
                         addExpiredPutDeadlines: Boolean = false)(implicit testTimer: TestTimer = TestTimer.random): Slice[Memory] =
    randomKeyValues(
      count = count,
      startId = startId,
      valueSize = valueSize,
      addPut = true,
      addRemoves = addRemoves,
      addRanges = addRanges,
      addExpiredPutDeadlines = addExpiredPutDeadlines,
      addRemoveDeadlines = addRemoveDeadlines,
      addPutDeadlines = addPutDeadlines
    ).toMemory

  def randomKeyValues(count: Int = 20,
                      startId: Option[Int] = None,
                      valueSize: Int = 50,
                      addPut: Boolean = true,
                      addRemoves: Boolean = false,
                      addRangeRemoves: Boolean = false,
                      addUpdates: Boolean = false,
                      addFunctions: Boolean = false,
                      addRemoveDeadlines: Boolean = false,
                      addPendingApply: Boolean = false,
                      addPutDeadlines: Boolean = false,
                      addExpiredPutDeadlines: Boolean = false,
                      addUpdateDeadlines: Boolean = false,
                      addRanges: Boolean = false,
                      valuesConfig: ValuesBlock.Config = ValuesBlock.Config.random,
                      sortedIndexConfig: SortedIndexBlock.Config = SortedIndexBlock.Config.random,
                      binarySearchIndexConfig: BinarySearchIndexBlock.Config = BinarySearchIndexBlock.Config.random,
                      hashIndexConfig: HashIndexBlock.Config = HashIndexBlock.Config.random,
                      bloomFilterConfig: BloomFilterBlock.Config = BloomFilterBlock.Config.random,
                      createdInLevel: Int = Int.MaxValue)(implicit testTimer: TestTimer = TestTimer.Incremental(),
                                                          keyOrder: KeyOrder[Slice[Byte]] = KeyOrder.default,
                                                          keyValueMemorySweeper: Option[MemorySweeper.KeyValue] = TestSweeper.memorySweeperMax): Slice[Transient] = {
    val slice = Slice.create[Transient](count * 50) //extra space because addRanges and random Groups can be added for Fixed and Range key-values in the same iteration.
    //            var key = 1
    var key = startId getOrElse randomInt(minus = count)
    var iteration = 0
    while (slice.size < count) {
      iteration += 1
      //      if (slice.written % 100000 == 0) println(s"Generated ${slice.written} key-values.")
      //protect from going into infinite loop
      if ((iteration >= count * 5) && slice.isEmpty) fail(s"Too many iterations ($iteration) without generated key-values. Expected $count.")

      if (addRanges && randomBoolean()) {
        val toKey = key + 10
        val fromValueValueBytes = eitherOne(None, Some(randomBytesSlice(valueSize)))
        val rangeValueValueBytes = eitherOne(None, Some(randomBytesSlice(valueSize)))
        val fromValueDeadline =
          if (addPutDeadlines || addRemoveDeadlines || addUpdateDeadlines)
            randomDeadlineOption(addExpiredPutDeadlines)
          else
            None
        val rangeValueDeadline = if (addRemoveDeadlines || addUpdateDeadlines) randomDeadlineOption else None
        slice add randomRangeKeyValue(
          from = key,
          to = toKey,
          fromValue = randomFromValueOption(value = fromValueValueBytes, deadline = fromValueDeadline, addPut = addPut),
          rangeValue = randomRangeValue(value = rangeValueValueBytes, addRemoves = addRangeRemoves, deadline = rangeValueDeadline)
        ).toTransient(
          previous = slice.lastOption,
          valuesConfig = valuesConfig,
          sortedIndexConfig = sortedIndexConfig,
          binarySearchIndexConfig = binarySearchIndexConfig,
          hashIndexConfig = hashIndexConfig,
          bloomFilterConfig = bloomFilterConfig
        )
        //randomly skip the Range's toKey for the next key.
        if (randomBoolean())
          key = toKey
        else
          key = toKey + randomIntMax(5)
      } else if (addRemoves && randomBoolean()) {
        slice add
          randomRemoveKeyValue(
            key = key: Slice[Byte],
            deadline = if (addRemoveDeadlines) randomDeadlineOption else None
          ).toTransient(
            previous = slice.lastOption,
            valuesConfig = valuesConfig,
            sortedIndexConfig = sortedIndexConfig,
            binarySearchIndexConfig = binarySearchIndexConfig,
            hashIndexConfig = hashIndexConfig,
            bloomFilterConfig = bloomFilterConfig
          )
        key = key + 1
      } else if (addUpdates && randomBoolean()) {
        val valueBytes = if (valueSize == 0) None else eitherOne(None, Some(randomBytesSlice(valueSize)))
        slice add
          randomUpdateKeyValue(
            key = key: Slice[Byte],
            deadline = if (addUpdateDeadlines) randomDeadlineOption else None,
            value = valueBytes
          ).toTransient(
            previous = slice.lastOption,
            valuesConfig = valuesConfig,
            sortedIndexConfig = sortedIndexConfig,
            binarySearchIndexConfig = binarySearchIndexConfig,
            hashIndexConfig = hashIndexConfig,
            bloomFilterConfig = bloomFilterConfig
          )
        key = key + 1
      } else if (addFunctions && randomBoolean()) {
        slice add
          randomFunctionKeyValue(
            key = key: Slice[Byte]
          ).toTransient(
            previous = slice.lastOption,
            valuesConfig = valuesConfig,
            sortedIndexConfig = sortedIndexConfig,
            binarySearchIndexConfig = binarySearchIndexConfig,
            hashIndexConfig = hashIndexConfig,
            bloomFilterConfig = bloomFilterConfig
          )
        key = key + 1
      } else if (addPendingApply && randomBoolean()) {
        val valueBytes = if (valueSize == 0) None else eitherOne(None, Some(randomBytesSlice(valueSize)))
        slice add
          randomPendingApplyKeyValue(
            key = key: Slice[Byte],
            deadline = if (addUpdateDeadlines) randomDeadlineOption else None,
            value = valueBytes
          ).toTransient(
            previous = slice.lastOption,
            valuesConfig = valuesConfig,
            sortedIndexConfig = sortedIndexConfig,
            binarySearchIndexConfig = binarySearchIndexConfig,
            hashIndexConfig = hashIndexConfig,
            bloomFilterConfig = bloomFilterConfig
          )
        key = key + 1
      } else if (addPut) {
        val valueBytes = if (valueSize == 0) None else eitherOne(None, Some(randomBytesSlice(valueSize)))
        val deadline = if (addPutDeadlines) randomDeadlineOption(addExpiredPutDeadlines) else None
        slice add
          randomPutKeyValue(
            key = key: Slice[Byte],
            deadline = deadline,
            value = valueBytes
          ).toTransient(
            slice.lastOption,
            valuesConfig = valuesConfig,
            sortedIndexConfig = sortedIndexConfig,
            binarySearchIndexConfig = binarySearchIndexConfig,
            hashIndexConfig = hashIndexConfig,
            bloomFilterConfig = bloomFilterConfig
          )
        key = key + 1
      } else {
        key = key + 1
      }
    }
    //    println(s"Generated: ${slice.size} over iterations: $iteration")
    slice.close()
  }

  def randomFixedNoneValue(count: Int = 20,
                           startId: Option[Int] = None,
                           addUpdates: Boolean = true,
                           addUpdateDeadlines: Boolean = true,
                           addPutDeadlines: Boolean = true,
                           addRemoves: Boolean = true,
                           addRemoveDeadlines: Boolean = true)(implicit testTimer: TestTimer = TestTimer.Incremental(),
                                                               keyOrder: KeyOrder[Slice[Byte]] = KeyOrder.default,
                                                               keyValueMemorySweeper: Option[MemorySweeper.KeyValue] = TestSweeper.memorySweeperMax): Slice[Transient] =
    randomKeyValues(
      count = count,
      startId = startId,
      valueSize = 0,
      addUpdates = addUpdates,
      addUpdateDeadlines = addUpdateDeadlines,
      addPutDeadlines = addPutDeadlines,
      addRemoves = addRemoves,
      addRemoveDeadlines = addRemoveDeadlines)

  implicit class MemoryTypeImplicits(memory: Memory.type) {

    def put(key: Slice[Byte],
            value: Slice[Byte])(implicit testTimer: TestTimer): Memory.Put =
      Memory.Put(key, Some(value), None, testTimer.next)

    def put(key: Slice[Byte],
            value: Slice[Byte],
            removeAt: Deadline)(implicit testTimer: TestTimer): Memory.Put =
      Memory.Put(key, Some(value), Some(removeAt), testTimer.next)

    def put(key: Slice[Byte],
            value: Option[Slice[Byte]],
            removeAt: Deadline)(implicit testTimer: TestTimer): Memory.Put =
      Memory.Put(key, value, Some(removeAt), testTimer.next)

    def put(key: Slice[Byte],
            value: Slice[Byte],
            removeAt: Option[Deadline])(implicit testTimer: TestTimer): Memory.Put =
      Memory.Put(key, Some(value), removeAt, testTimer.next)

    def put(key: Slice[Byte],
            value: Slice[Byte],
            removeAfter: FiniteDuration)(implicit testTimer: TestTimer): Memory.Put =
      Memory.Put(key, Some(value), Some(removeAfter.fromNow), testTimer.next)

    def put(key: Slice[Byte],
            value: Option[Slice[Byte]])(implicit testTimer: TestTimer): Memory.Put =
      Memory.Put(key, value, None, testTimer.next)

    def put(key: Slice[Byte])(implicit testTimer: TestTimer): Memory.Put =
      Memory.Put(key, None, None, testTimer.next)

    def put(key: Slice[Byte],
            value: Option[Slice[Byte]],
            deadline: Option[Deadline],
            time: Time): Memory.Put =
      Memory.Put(key, value, deadline, time)

    def put(key: Slice[Byte],
            value: Option[Slice[Byte]],
            deadline: Option[Deadline])(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Put =
      Memory.Put(key, value, deadline, testTimer.next)

    def update(key: Slice[Byte],
               value: Slice[Byte])(implicit testTimer: TestTimer): Memory.Update =
      Memory.Update(key, Some(value), None, testTimer.next)

    def update(key: Slice[Byte],
               value: Slice[Byte],
               removeAt: Deadline)(implicit testTimer: TestTimer): Memory.Update =
      Memory.Update(key, Some(value), Some(removeAt), testTimer.next)

    def update(key: Slice[Byte],
               value: Option[Slice[Byte]],
               removeAt: Deadline)(implicit testTimer: TestTimer): Memory.Update =
      Memory.Update(key, value, Some(removeAt), testTimer.next)

    def update(key: Slice[Byte],
               value: Slice[Byte],
               removeAt: Option[Deadline])(implicit testTimer: TestTimer): Memory.Update =
      Memory.Update(key, Some(value), removeAt, testTimer.next)

    def update(key: Slice[Byte],
               value: Slice[Byte],
               removeAfter: FiniteDuration)(implicit testTimer: TestTimer): Memory.Update =
      Memory.Update(key, Some(value), Some(removeAfter.fromNow), testTimer.next)

    def update(key: Slice[Byte],
               value: Option[Slice[Byte]])(implicit testTimer: TestTimer): Memory.Update =
      Memory.Update(key, value, None, testTimer.next)

    def update(key: Slice[Byte])(implicit testTimer: TestTimer): Memory.Update =
      Memory.Update(key, None, None, testTimer.next)

    def update(key: Slice[Byte],
               value: Option[Slice[Byte]],
               deadline: Option[Deadline])(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Update =
      Memory.Update(key, value, deadline, testTimer.next)

    def remove(key: Slice[Byte]): Memory.Remove =
      Memory.Remove(key, None, Time.empty)

    def remove(key: Slice[Byte], deadline: Deadline): Memory.Remove =
      Memory.Remove(key, Some(deadline), Time.empty)

    def remove(key: Slice[Byte], deadline: FiniteDuration): Memory.Remove =
      Memory.Remove(key, Some(deadline.fromNow), Time.empty)

    def remove(key: Slice[Byte],
               deadline: Option[Deadline])(implicit testTimer: TestTimer = TestTimer.Incremental()): Memory.Remove =
      Memory.Remove(key, deadline, testTimer.next)
  }

  implicit class TransientTypeImplicits(transient: Transient.type) {

    def remove(key: Slice[Byte])(implicit testTimer: TestTimer): Transient.Remove =
      Transient.Remove(
        key = key,
        normaliseToSize = None,
        deadline = None,
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )

    def remove(key: Slice[Byte],
               removeAfter: FiniteDuration)(implicit testTimer: TestTimer): Transient.Remove =
      Transient.Remove(
        key = key,
        normaliseToSize = None,
        deadline = Some(removeAfter.fromNow),
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )

    def remove(key: Slice[Byte],
               previous: Option[Transient])(implicit testTimer: TestTimer): Transient.Remove =
      Transient.Remove(
        key = key,
        normaliseToSize = None,
        deadline = None,
        time = testTimer.next,
        valuesConfig = previous.map(_.valuesConfig).getOrElse(ValuesBlock.Config.random),
        sortedIndexConfig = previous.map(_.sortedIndexConfig).getOrElse(SortedIndexBlock.Config.random),
        binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig).getOrElse(BinarySearchIndexBlock.Config.random),
        hashIndexConfig = previous.map(_.hashIndexConfig).getOrElse(HashIndexBlock.Config.random),
        bloomFilterConfig = previous.map(_.bloomFilterConfig).getOrElse(BloomFilterBlock.Config.random),
        previous = previous
      )

    def remove(key: Slice[Byte],
               previous: Option[Transient],
               deadline: Option[Deadline])(implicit testTimer: TestTimer): Transient.Remove =
      Transient.Remove(
        key = key,
        normaliseToSize = None,
        deadline = deadline,
        time = testTimer.next,
        previous = previous,
        valuesConfig = previous.map(_.valuesConfig).getOrElse(ValuesBlock.Config.random),
        sortedIndexConfig = previous.map(_.sortedIndexConfig).getOrElse(SortedIndexBlock.Config.random),
        binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig).getOrElse(BinarySearchIndexBlock.Config.random),
        hashIndexConfig = previous.map(_.hashIndexConfig).getOrElse(HashIndexBlock.Config.random),
        bloomFilterConfig = previous.map(_.bloomFilterConfig).getOrElse(BloomFilterBlock.Config.random)
      )

    def put(key: Slice[Byte],
            value: Option[Slice[Byte]],
            previous: Option[Transient])(implicit testTimer: TestTimer): Transient.Put =
      Transient.Put(
        key = key,
        normaliseToSize = None,
        value = value,
        deadline = None,
        time = testTimer.next,
        valuesConfig = previous.map(_.valuesConfig).getOrElse(ValuesBlock.Config.random),
        sortedIndexConfig = previous.map(_.sortedIndexConfig).getOrElse(SortedIndexBlock.Config.random),
        binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig).getOrElse(BinarySearchIndexBlock.Config.random),
        hashIndexConfig = previous.map(_.hashIndexConfig).getOrElse(HashIndexBlock.Config.random),
        bloomFilterConfig = previous.map(_.bloomFilterConfig).getOrElse(BloomFilterBlock.Config.random),
        previous = previous
      )

    def put(key: Slice[Byte],
            value: Option[Slice[Byte]],
            previous: Option[Transient],
            deadline: Option[Deadline],
            compressDuplicateValues: Boolean)(implicit testTimer: TestTimer): Transient.Put =
      Transient.Put(
        key = key,
        normaliseToSize = None,
        value = value,
        deadline = deadline,
        time = testTimer.next,
        valuesConfig = previous.map(_.valuesConfig).getOrElse(ValuesBlock.Config.random),
        sortedIndexConfig = previous.map(_.sortedIndexConfig).getOrElse(SortedIndexBlock.Config.random),
        binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig).getOrElse(BinarySearchIndexBlock.Config.random),
        hashIndexConfig = previous.map(_.hashIndexConfig).getOrElse(HashIndexBlock.Config.random),
        bloomFilterConfig = previous.map(_.bloomFilterConfig).getOrElse(BloomFilterBlock.Config.random),
        previous = previous
      )

    def put(key: Slice[Byte])(implicit testTimer: TestTimer): Transient.Put =
      Transient.Put(
        key = key,
        normaliseToSize = None,
        value = None,
        deadline = None,
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )

    def put(key: Slice[Byte],
            value: Slice[Byte])(implicit testTimer: TestTimer): Transient.Put =
      Transient.Put(
        key = key,
        normaliseToSize = None,
        value = Some(value),
        deadline = None,
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )

    def put(key: Slice[Byte],
            value: Slice[Byte],
            removeAfter: FiniteDuration)(implicit testTimer: TestTimer): Transient.Put =
      Transient.Put(
        key = key,
        normaliseToSize = None,
        value = Some(value),
        deadline = Some(removeAfter.fromNow),
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )

    def put(key: Slice[Byte],
            value: Slice[Byte],
            deadline: Deadline)(implicit testTimer: TestTimer): Transient.Put =
      Transient.Put(
        key = key,
        normaliseToSize = None,
        value = Some(value),
        deadline = Some(deadline),
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )

    def put(key: Slice[Byte],
            removeAfter: FiniteDuration)(implicit testTimer: TestTimer): Transient.Put =
      Transient.Put(
        key = key,
        normaliseToSize = None,
        value = None,
        deadline = Some(removeAfter.fromNow),
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )

    def put(key: Slice[Byte],
            value: Slice[Byte],
            removeAfter: Option[FiniteDuration])(implicit testTimer: TestTimer): Transient.Put =
      Transient.Put(
        key = key,
        normaliseToSize = None,
        value = Some(value),
        deadline = removeAfter.map(_.fromNow),
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )

    def update(key: Slice[Byte],
               value: Option[Slice[Byte]],
               previous: Option[Transient])(implicit testTimer: TestTimer): Transient.Update =
      Transient.Update(
        key = key,
        normaliseToSize = None,
        value = value,
        deadline = None,
        time = testTimer.next,
        valuesConfig = previous.map(_.valuesConfig).getOrElse(ValuesBlock.Config.random),
        sortedIndexConfig = previous.map(_.sortedIndexConfig).getOrElse(SortedIndexBlock.Config.random),
        binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig).getOrElse(BinarySearchIndexBlock.Config.random),
        hashIndexConfig = previous.map(_.hashIndexConfig).getOrElse(HashIndexBlock.Config.random),
        bloomFilterConfig = previous.map(_.bloomFilterConfig).getOrElse(BloomFilterBlock.Config.random),
        previous = previous
      )

    def update(key: Slice[Byte],
               value: Option[Slice[Byte]],
               previous: Option[Transient],
               deadline: Option[Deadline])(implicit testTimer: TestTimer): Transient.Update =
      Transient.Update(
        key = key,
        normaliseToSize = None,
        value = value,
        deadline = deadline,
        time = testTimer.next,
        valuesConfig = previous.map(_.valuesConfig).getOrElse(ValuesBlock.Config.random),
        sortedIndexConfig = previous.map(_.sortedIndexConfig).getOrElse(SortedIndexBlock.Config.random),
        binarySearchIndexConfig = previous.map(_.binarySearchIndexConfig).getOrElse(BinarySearchIndexBlock.Config.random),
        hashIndexConfig = previous.map(_.hashIndexConfig).getOrElse(HashIndexBlock.Config.random),
        bloomFilterConfig = previous.map(_.bloomFilterConfig).getOrElse(BloomFilterBlock.Config.random),
        previous = previous
      )

    def update(key: Slice[Byte])(implicit testTimer: TestTimer): Transient.Update =
      Transient.Update(
        key = key,
        normaliseToSize = None,
        value = None,
        deadline = None,
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )

    def update(key: Slice[Byte],
               value: Slice[Byte])(implicit testTimer: TestTimer): Transient.Update =
      Transient.Update(
        key = key,
        normaliseToSize = None,
        value = Some(value),
        deadline = None,
        previous = None,
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random
      )

    def update(key: Slice[Byte],
               value: Slice[Byte],
               removeAfter: FiniteDuration)(implicit testTimer: TestTimer): Transient.Update =
      Transient.Update(
        key = key,
        normaliseToSize = None,
        value = Some(value),
        deadline = Some(removeAfter.fromNow),
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )

    def update(key: Slice[Byte],
               value: Slice[Byte],
               deadline: Deadline)(implicit testTimer: TestTimer): Transient.Update =
      Transient.Update(
        key = key,
        normaliseToSize = None,
        value = Some(value),
        deadline = Some(deadline),
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )

    def update(key: Slice[Byte],
               removeAfter: FiniteDuration)(implicit testTimer: TestTimer): Transient.Update =
      Transient.Update(
        key = key,
        normaliseToSize = None,
        value = None,
        deadline = Some(removeAfter.fromNow),
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )

    def update(key: Slice[Byte],
               value: Slice[Byte],
               removeAfter: Option[FiniteDuration])(implicit testTimer: TestTimer): Transient.Update =
      Transient.Update(
        key = key,
        normaliseToSize = None,
        value = Some(value),
        deadline = removeAfter.map(_.fromNow),
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )

    def function(key: Slice[Byte],
                 function: Slice[Byte])(implicit testTimer: TestTimer): Transient.Function =
      Transient.Function(
        key = key,
        normaliseToSize = None,
        function = function,
        time = testTimer.next,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = BloomFilterBlock.Config.random,
        previous = None
      )
  }

  implicit class ValueUpdateTypeImplicits(remove: Value.type) {

    def remove(deadline: Option[Deadline],
               time: Time): Value.Remove =
      Value.Remove(deadline, time)

    def remove(deadline: Deadline)(implicit testTimer: TestTimer): Value.Remove =
      Value.Remove(Some(deadline), testTimer.next)

    def remove(deadline: Option[Deadline])(implicit testTimer: TestTimer): Value.Remove =
      Value.Remove(deadline, testTimer.next)

    def put(value: Option[Slice[Byte]],
            deadline: Option[Deadline],
            time: Time)(implicit testTimer: TestTimer): Value.Put =
      Value.Put(Some(value), deadline, time)

    def put(value: Slice[Byte])(implicit testTimer: TestTimer): Value.Put =
      Value.Put(Some(value), None, testTimer.next)

    def put(value: Option[Slice[Byte]])(removeAfter: Deadline)(implicit testTimer: TestTimer): Value.Put =
      Value.Put(value, Some(removeAfter), testTimer.next)

    def put(value: Slice[Byte], removeAfter: Deadline)(implicit testTimer: TestTimer): Value.Put =
      Value.Put(Some(value), Some(removeAfter), testTimer.next)

    def put(value: Option[Slice[Byte]], removeAfter: Option[Deadline])(implicit testTimer: TestTimer): Value.Put =
      Value.Put(value, removeAfter, testTimer.next)

    def put(value: Slice[Byte], duration: FiniteDuration)(implicit testTimer: TestTimer): Value.Put =
      Value.Put(Some(value), Some(duration.fromNow), testTimer.next)

    def put(value: Option[Slice[Byte]], duration: FiniteDuration)(implicit testTimer: TestTimer): Value.Put =
      Value.Put(value, Some(duration.fromNow), testTimer.next)

    def update(value: Option[Slice[Byte]],
               deadline: Option[Deadline],
               time: Time): Value.Update =
      Value.Update(Some(value), deadline, time)

    def update(value: Slice[Byte])(implicit testTimer: TestTimer): Value.Update =
      Value.Update(Some(value), None, testTimer.next)

    def update(value: Slice[Byte], deadline: Option[Deadline])(implicit testTimer: TestTimer): Value.Update =
      Value.Update(Some(value), deadline, testTimer.next)

    def update(value: Option[Slice[Byte]])(removeAfter: Deadline)(implicit testTimer: TestTimer): Value.Update =
      Value.Update(value, Some(removeAfter), testTimer.next)

    def update(value: Slice[Byte], removeAfter: Deadline)(implicit testTimer: TestTimer): Value.Update =
      Value.Update(Some(value), Some(removeAfter), testTimer.next)

    def update(value: Option[Slice[Byte]], removeAfter: Option[Deadline])(implicit testTimer: TestTimer): Value.Update =
      Value.Update(value, removeAfter, testTimer.next)

    def update(value: Slice[Byte], duration: FiniteDuration)(implicit testTimer: TestTimer): Value.Update =
      Value.Update(Some(value), Some(duration.fromNow), testTimer.next)

    def update(value: Option[Slice[Byte]], duration: FiniteDuration)(implicit testTimer: TestTimer): Value.Update =
      Value.Update(value, Some(duration.fromNow), testTimer.next)
  }

  implicit class RangeTypeImplicits(range: Transient.Range.type) {
    def create[F <: Value.FromValue, R <: Value.RangeValue](fromKey: Slice[Byte],
                                                            toKey: Slice[Byte],
                                                            fromValue: Option[F],
                                                            rangeValue: R,
                                                            bloomFilterConfig: BloomFilterBlock.Config = BloomFilterBlock.Config.random)(implicit rangeValueSerializer: RangeValueSerializer[Option[F], R]): Range =
      Range(
        fromKey = fromKey,
        toKey = toKey,
        fromValue = fromValue,
        rangeValue = rangeValue,
        valuesConfig = ValuesBlock.Config.random,
        sortedIndexConfig = SortedIndexBlock.Config.random,
        binarySearchIndexConfig = BinarySearchIndexBlock.Config.random,
        hashIndexConfig = HashIndexBlock.Config.random,
        bloomFilterConfig = bloomFilterConfig,
        previous = None
      )
  }

  def collectUsedDeadlines(keyValues: Slice[Transient], usedDeadlines: List[Deadline]): List[Deadline] =
    keyValues.foldLeft(usedDeadlines) {
      case (usedDeadlines, keyValue) =>
        keyValue match {
          case remove: Transient.Remove =>
            usedDeadlines ++ remove.deadline
          case put: Transient.Put =>
            usedDeadlines ++ put.deadline
          case update: Transient.Update =>
            usedDeadlines ++ update.deadline
          case _: Transient.Function =>
            usedDeadlines
          case apply: Transient.PendingApply =>
            collectUsedDeadlines(apply.applies.map(_.toMemory(Slice.emptyBytes)).map(_.toTransient), usedDeadlines)
          case range: Transient.Range =>
            val fromTransient = range.fromValue.map(_.toMemory(Slice.emptyBytes).toTransient)
            val rangeTransient = range.rangeValue.toMemory(Slice.emptyBytes).toTransient
            collectUsedDeadlines(Slice(rangeTransient) ++ fromTransient, usedDeadlines)
        }
    }

  def nearestDeadline(keyValues: Slice[Transient]): Option[Deadline] = {
    val usedDeadlines = collectUsedDeadlines(keyValues.toSlice, List.empty)
    if (usedDeadlines.isEmpty)
      None
    else
      Some(
        usedDeadlines.reduce[Deadline] {
          case (left, right) =>
            if (left < right)
              left
            else
              right
        }
      )
  }

  def maxKey(keyValues: Slice[Transient]): MaxKey[Slice[Byte]] =
    getMaxKey(keyValues.last)

  def getMaxKey(transient: Transient): MaxKey[Slice[Byte]] =
    transient match {
      case last: Transient.Remove =>
        MaxKey.Fixed(last.key)
      case last: Transient.Put =>
        MaxKey.Fixed(last.key)
      case last: Transient.Update =>
        MaxKey.Fixed(last.key)
      case last: Transient.Function =>
        MaxKey.Fixed(last.key)
      case last: Transient.PendingApply =>
        MaxKey.Fixed(last.key)
      case last: Transient.Range =>
        MaxKey.Range(last.fromKey, last.toKey)
    }

  def unexpiredPuts(keyValues: Iterable[KeyValue]): Slice[KeyValue.ReadOnly.Put] = {
    val slice = Slice.create[KeyValue.ReadOnly.Put](keyValues.size)
    keyValues foreach {
      keyValue =>
        keyValue.asPut foreach {
          put =>
            if (put.hasTimeLeft())
              slice add put
        }
    }
    slice
  }

  def getPuts(keyValues: Iterable[KeyValue]): Slice[KeyValue.ReadOnly.Put] = {
    val slice = Slice.create[KeyValue.ReadOnly.Put](keyValues.size)
    keyValues foreach {
      keyValue =>
        keyValue.asPut foreach slice.add
    }
    slice
  }

  /**
   * Randomly updates all key-values using one of the many update methods.
   *
   * Used for testing all updates work for all existing put key-values.
   */
  def randomUpdate(keyValues: Iterable[KeyValue.ReadOnly.Put],
                   updatedValue: Option[Slice[Byte]],
                   deadline: Option[Deadline],
                   randomlyDropUpdates: Boolean)(implicit testTimer: TestTimer = TestTimer.Incremental()): Slice[Memory] = {
    var keyUsed = keyValues.head.key.readInt() - 1
    val updateSlice = Slice.create[Memory](keyValues.size)

    keyValues foreach {
      keyValue =>
        if (randomlyDropUpdates && randomBoolean()) {
          keyUsed = keyValue.key.readInt()
        } else if (keyUsed < keyValue.key.readInt()) {
          eitherOne(
            left = {
              keyUsed = keyValue.key.readInt()
              Some(randomUpdateKeyValue(keyValue.key, updatedValue, deadline = deadline))
            },
            mid = {
              keyUsed = keyValue.key.readInt() + 10
              Some(
                randomRangeKeyValue(
                  from = keyValue.key,
                  to = keyUsed + 1,
                  fromValue = randomFromValueOption(
                    value = updatedValue,
                    deadline = deadline,
                    addRemoves = false,
                    functionOutput = SwayFunctionOutput.Update(updatedValue, deadline),
                    addPut = false
                  ),
                  rangeValue = randomRangeValue(
                    value = updatedValue,
                    deadline = deadline,
                    functionOutput = SwayFunctionOutput.Update(updatedValue, deadline),
                    addRemoves = false
                  )
                )
              )
            },
            right = {
              keyUsed = keyValue.key.readInt()
              Some(
                randomFunctionKeyValue(
                  key = keyValue.key,
                  output = SwayFunctionOutput.Update(updatedValue, deadline)
                )
              )
            }
          ) foreach updateSlice.add
        }
    }

    updateSlice
  }

  implicit class HigherImplicits(higher: Higher.type) {
    def apply(key: Slice[Byte])(implicit keyOrder: KeyOrder[Slice[Byte]],
                                timeOrder: TimeOrder[Slice[Byte]],
                                currentReader: CurrentWalker,
                                nextReader: NextWalker,
                                functionStore: FunctionStore): IO[swaydb.Error.Level, Option[KeyValue.ReadOnly.Put]] =
      Higher(key, ReadState.random, Seek.Current.Read(Int.MinValue), Seek.Next.Read).runIO
  }

  implicit class LowerImplicits(higher: Lower.type) {
    def apply(key: Slice[Byte])(implicit keyOrder: KeyOrder[Slice[Byte]],
                                timeOrder: TimeOrder[Slice[Byte]],
                                currentReader: CurrentWalker,
                                nextReader: NextWalker,
                                functionStore: FunctionStore): IO[swaydb.Error.Level, Option[KeyValue.ReadOnly.Put]] =
      Lower(key, ReadState.random, Seek.Current.Read(Int.MinValue), Seek.Next.Read).runIO
  }

  def randomStats(unmergedKeySize: Int = randomIntMax(10000000),
                  mergedKeySize: Int = randomIntMax(10000000),
                  keyOffset: Int = randomIntMax(10000000),
                  indexEntry: Slice[Byte] = randomBytesSlice(),
                  value: Option[Slice[Byte]] = randomBytesSliceOption(),
                  isRemoveRange: Boolean = randomBoolean(),
                  isRange: Boolean = randomBoolean(),
                  isPut: Boolean = randomBoolean(),
                  isPrefixCompressed: Boolean = randomBoolean(),
                  sortedIndex: SortedIndexBlock.Config = SortedIndexBlock.Config.random,
                  bloomFilter: BloomFilterBlock.Config = BloomFilterBlock.Config.random,
                  hashIndex: HashIndexBlock.Config = HashIndexBlock.Config.random,
                  binarySearch: BinarySearchIndexBlock.Config = BinarySearchIndexBlock.Config.random,
                  values: ValuesBlock.Config = ValuesBlock.Config.random,
                  previousStats: Option[Stats] = None,
                  deadline: Option[Deadline] = randomDeadlineOption()): Stats =
    Stats(
      unmergedKeySize = unmergedKeySize,
      mergedKeySize = mergedKeySize,
      keyOffset = keyOffset,
      indexEntry = indexEntry,
      value = value,
      isRemoveRange = isRemoveRange,
      isRange = isRange,
      isPut = isPut,
      isPrefixCompressed = isPrefixCompressed,
      sortedIndex = sortedIndex,
      bloomFilter = bloomFilter,
      hashIndex = hashIndex,
      binarySearch = binarySearch,
      values = values,
      previousStats = previousStats,
      deadline = deadline
    )

  def randomFalsePositiveRate() =
    Random.nextDouble()

  def randomIOAccess(cacheOnAccess: => Boolean = randomBoolean()) =
    Random.shuffle(
      Seq(
        IOStrategy.ConcurrentIO(cacheOnAccess),
        IOStrategy.SynchronisedIO(cacheOnAccess),
        IOStrategy.AsyncIO(cacheOnAccess = true)
      )
    ).head

  def randomBinarySearchFormat(): BinarySearchEntryFormat =
    Random.shuffle(BinarySearchEntryFormat.formats.toList).head

  def randomHashIndexSearchFormat(): HashIndexEntryFormat =
    Random.shuffle(HashIndexEntryFormat.formats.toList).head

  implicit class SegmentBlockImplicits(segmentBlock: SegmentBlock.type) {

    def emptyDecompressedBlock: UnblockedReader[SegmentBlock.Offset, SegmentBlock] =
      UnblockedReader.empty(
        SegmentBlock(
          offset = SegmentBlock.Offset.empty,
          headerSize = 0,
          compressionInfo = None
        )
      )

    def unblocked(bytes: Slice[Byte])(implicit updater: BlockOps[SegmentBlock.Offset, SegmentBlock]): UnblockedReader[SegmentBlock.Offset, SegmentBlock] =
      UnblockedReader(
        block =
          SegmentBlock(
            offset = SegmentBlock.Offset(
              start = 0,
              size = bytes.size
            ),
            headerSize = 0,
            compressionInfo = None
          ),
        bytes = bytes
      )

    def blocked(bytes: Slice[Byte], headerSize: Int, compressionInfo: Block.CompressionInfo)(implicit updater: BlockOps[SegmentBlock.Offset, SegmentBlock]): BlockedReader[SegmentBlock.Offset, SegmentBlock] =
      BlockedReader(
        bytes = bytes,
        block =
          SegmentBlock(
            offset = SegmentBlock.Offset(
              start = 0,
              size = bytes.size
            ),
            headerSize = headerSize,
            compressionInfo = Some(compressionInfo)
          )
      )
  }

  def buildSingleValueCache(bytes: Slice[Byte]): Cache[swaydb.Error.Segment, ValuesBlock.Offset, UnblockedReader[ValuesBlock.Offset, ValuesBlock]] =
    Cache.concurrentIO[swaydb.Error.Segment, ValuesBlock.Offset, UnblockedReader[ValuesBlock.Offset, ValuesBlock]](randomBoolean(), randomBoolean(), None) {
      (offset, _) =>
        IO[Nothing, UnblockedReader[ValuesBlock.Offset, ValuesBlock]](
          UnblockedReader(
            block = ValuesBlock(offset, 0, None),
            bytes = bytes
          )
        )(Nothing)
    }

  def buildSingleValueReader(bytes: Slice[Byte]): UnblockedReader[ValuesBlock.Offset, ValuesBlock] =
    UnblockedReader(
      block = ValuesBlock(ValuesBlock.Offset(0, bytes.size), 0, None),
      bytes = bytes
    )

  implicit class ActorConfigImplicits(actorConfig: ActorConfig.type) {
    def random(delay: FiniteDuration = 2.seconds)(implicit ec: ExecutionContext): ActorConfig =
      if (randomBoolean())
        ActorConfig.Basic(ec)
      else if (randomBoolean())
        ActorConfig.Timer(delay, ec)
      else
        ActorConfig.TimeLoop(delay, ec)
  }

}
