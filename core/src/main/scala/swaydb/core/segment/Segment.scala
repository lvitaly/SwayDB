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

package swaydb.core.segment

import java.nio.file.{Path, Paths}

import com.typesafe.scalalogging.LazyLogging
import swaydb.Error.Segment.ExceptionHandler
import swaydb.IO
import swaydb.IO._
import swaydb.core.actor.{FileSweeper, FileSweeperItem, MemorySweeper}
import swaydb.core.data._
import swaydb.core.function.FunctionStore
import swaydb.core.io.file.{BlockCache, DBFile, Effect}
import swaydb.core.level.PathsDistributor
import swaydb.core.map.Map
import swaydb.core.segment.format.a.block._
import swaydb.core.segment.format.a.block.binarysearch.BinarySearchIndexBlock
import swaydb.core.segment.format.a.block.hashindex.HashIndexBlock
import swaydb.core.segment.format.a.block.reader.BlockRefReader
import swaydb.core.segment.merge.SegmentMerger
import swaydb.core.util.Collections._
import swaydb.core.util._
import swaydb.data.MaxKey
import swaydb.data.config.{Dir, IOAction}
import swaydb.data.order.{KeyOrder, TimeOrder}
import swaydb.data.slice.Slice

import scala.jdk.CollectionConverters._
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Deadline

private[core] object Segment extends LazyLogging {

  val emptyIterable = Iterable.empty[Segment]
  val emptyIterableIO = IO.Right[Nothing, Iterable[Segment]](emptyIterable)(swaydb.IO.ExceptionHandler.Nothing)

  def memory(path: Path,
             segmentId: Long,
             createdInLevel: Long,
             keyValues: Iterable[Transient])(implicit keyOrder: KeyOrder[Slice[Byte]],
                                             timeOrder: TimeOrder[Slice[Byte]],
                                             functionStore: FunctionStore,
                                             fileSweeper: FileSweeper.Enabled): Segment =
    if (keyValues.isEmpty) {
      throw IO.throwable("Empty key-values submitted to memory Segment.")
    } else {
      val bloomFilterOption: Option[BloomFilterBlock.State] = BloomFilterBlock.init(keyValues = keyValues)
      val skipList = SkipList.immutable[Slice[Byte], Memory]()(keyOrder)

      //Note: Transient key-values can be received from Persistent Segments in which case it's important that
      //all byte arrays are unsliced before writing them to Memory Segment.

      val minMaxDeadline =
        keyValues.foldLeft(DeadlineAndFunctionId.empty) {
          case (deadline, keyValue) =>
            SegmentBlock.writeIndexBlocks(
              keyValue = keyValue,
              skipList = Some(skipList),
              hashIndex = None,
              binarySearchIndex = None,
              bloomFilter = bloomFilterOption,
              currentMinMaxFunction = deadline.minMaxFunctionId,
              currentNearestDeadline = deadline.nearestDeadline
            )
        }

      val bloomFilter = bloomFilterOption.flatMap(BloomFilterBlock.closeForMemory)

      MemorySegment(
        path = path,
        segmentId = segmentId,
        minKey = keyValues.head.key.unslice(),
        maxKey =
          keyValues.last match {
            case range: Transient.Range =>
              MaxKey.Range(range.fromKey.unslice(), range.toKey.unslice())

            case keyValue: Transient.Fixed =>
              MaxKey.Fixed(keyValue.key.unslice())
          },
        minMaxFunctionId = minMaxDeadline.minMaxFunctionId,
        segmentSize = keyValues.last.stats.memorySegmentSize,
        hasRange = keyValues.last.stats.segmentHasRange,
        hasPut = keyValues.last.stats.segmentHasPut,
        createdInLevel = createdInLevel.toInt,
        skipList = skipList,
        bloomFilterReader = bloomFilter,
        nearestExpiryDeadline = minMaxDeadline.nearestDeadline
      )
    }

  def persistent(path: Path,
                 segmentId: Long,
                 createdInLevel: Int,
                 mmapReads: Boolean,
                 mmapWrites: Boolean,
                 segmentConfig: SegmentBlock.Config,
                 keyValues: Iterable[Transient])(implicit keyOrder: KeyOrder[Slice[Byte]],
                                                 timeOrder: TimeOrder[Slice[Byte]],
                                                 functionStore: FunctionStore,
                                                 fileSweeper: FileSweeper.Enabled,
                                                 keyValueMemorySweeper: Option[MemorySweeper.KeyValue],
                                                 blockCache: Option[BlockCache.State],
                                                 segmentIO: SegmentIO): Segment = {
    val result =
      SegmentBlock.writeClosed(
        keyValues = keyValues,
        createdInLevel = createdInLevel,
        segmentConfig = segmentConfig
      )

    if (result.isEmpty) {
      //This is fatal!! Empty Segments should never be created. If this does have for whatever reason it should
      //not be allowed so that whatever is creating this Segment (eg: compaction) does not progress with a success response.
      throw IO.throwable("Empty key-values submitted to persistent Segment.")
    } else {
      val file =
      //if both read and writes are mmaped. Keep the file open.
        if (mmapWrites && mmapReads)
          DBFile.mmapWriteAndRead(
            path = path,
            autoClose = true,
            ioStrategy = segmentIO.segmentBlockIO(IOAction.OpenResource),
            blockCacheFileId = BlockCacheFileIDGenerator.nextID,
            bytes = result.segmentBytes
          )
        //if mmapReads is false, write bytes in mmaped mode and then close and re-open for read.
        else if (mmapWrites && !mmapReads) {
          val file =
            DBFile.mmapWriteAndRead(
              path = path,
              autoClose = true,
              ioStrategy = segmentIO.segmentBlockIO(IOAction.OpenResource),
              blockCacheFileId = BlockCacheFileIDGenerator.nextID,
              bytes = result.segmentBytes
            )

          //close immediately to force flush the bytes to disk. Having mmapWrites == true and mmapReads == false,
          //is probably not the most efficient and should be advised not to used.
          file.close()
          DBFile.channelRead(
            path = file.path,
            ioStrategy = segmentIO.segmentBlockIO(IOAction.OpenResource),
            blockCacheFileId = BlockCacheFileIDGenerator.nextID,
            autoClose = true
          )
        }
        else if (!mmapWrites && mmapReads)
          DBFile.mmapRead(
            path = Effect.write(path, result.segmentBytes),
            ioStrategy = segmentIO.segmentBlockIO(IOAction.OpenResource),
            blockCacheFileId = BlockCacheFileIDGenerator.nextID,
            autoClose = true
          )
        else
          DBFile.channelRead(
            path = Effect.write(path, result.segmentBytes),
            ioStrategy = segmentIO.segmentBlockIO(IOAction.OpenResource),
            blockCacheFileId = BlockCacheFileIDGenerator.nextID,
            autoClose = true
          )

      PersistentSegment(
        file = file,
        segmentId = segmentId,
        mmapReads = mmapReads,
        mmapWrites = mmapWrites,
        minKey = keyValues.head.key.unslice(),
        maxKey =
          keyValues.last match {
            case range: Transient.Range =>
              MaxKey.Range(range.fromKey.unslice(), range.toKey.unslice())

            case keyValue: Transient.Fixed =>
              MaxKey.Fixed(keyValue.key.unslice())
          },
        segmentSize = result.segmentSize,
        minMaxFunctionId = result.minMaxFunctionId,
        nearestExpiryDeadline = result.nearestDeadline
      )
    }
  }

  def copyToPersist(segment: Segment,
                    segmentConfig: SegmentBlock.Config,
                    createdInLevel: Int,
                    fetchNextPath: => (Long, Path),
                    mmapSegmentsOnRead: Boolean,
                    mmapSegmentsOnWrite: Boolean,
                    removeDeletes: Boolean,
                    minSegmentSize: Long,
                    valuesConfig: ValuesBlock.Config,
                    sortedIndexConfig: SortedIndexBlock.Config,
                    binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                    hashIndexConfig: HashIndexBlock.Config,
                    bloomFilterConfig: BloomFilterBlock.Config)(implicit keyOrder: KeyOrder[Slice[Byte]],
                                                                timeOrder: TimeOrder[Slice[Byte]],
                                                                functionStore: FunctionStore,
                                                                keyValueMemorySweeper: Option[MemorySweeper.KeyValue],
                                                                fileSweeper: FileSweeper.Enabled,
                                                                blockCache: Option[BlockCache.State],
                                                                segmentIO: SegmentIO): Slice[Segment] =
    segment match {
      case segment: PersistentSegment =>
        val (segmentId, nextPath) = fetchNextPath
        segment.copyTo(nextPath)
        try
          Slice(
            Segment(
              path = nextPath,
              segmentId = segmentId,
              blockCacheFileId = segment.file.blockCacheFileId,
              mmapReads = mmapSegmentsOnRead,
              mmapWrites = mmapSegmentsOnWrite,
              minKey = segment.minKey,
              maxKey = segment.maxKey,
              segmentSize = segment.segmentSize,
              minMaxFunctionId = segment.minMaxFunctionId,
              nearestExpiryDeadline = segment.nearestExpiryDeadline
            )
          )
        catch {
          case exception: Exception =>
            logger.error("Failed to copyToPersist Segment {}", segment.path, exception)
            IO(Effect.deleteIfExists(nextPath)) onLeftSideEffect {
              exception =>
                logger.error("Failed to delete copied persistent Segment {}", segment.path, exception)
            }
            throw exception
        }

      case memory: MemorySegment =>
        copyToPersist(
          keyValues = memory.skipList.values().asScala,
          segmentConfig = segmentConfig,
          createdInLevel = createdInLevel,
          fetchNextPath = fetchNextPath,
          mmapSegmentsOnRead = mmapSegmentsOnRead,
          mmapSegmentsOnWrite = mmapSegmentsOnWrite,
          removeDeletes = removeDeletes,
          minSegmentSize = minSegmentSize,
          valuesConfig = valuesConfig,
          sortedIndexConfig = sortedIndexConfig,
          binarySearchIndexConfig = binarySearchIndexConfig,
          hashIndexConfig = hashIndexConfig,
          bloomFilterConfig = bloomFilterConfig
        )
    }

  def copyToPersist(keyValues: Iterable[KeyValue.ReadOnly],
                    segmentConfig: SegmentBlock.Config,
                    createdInLevel: Int,
                    fetchNextPath: => (Long, Path),
                    mmapSegmentsOnRead: Boolean,
                    mmapSegmentsOnWrite: Boolean,
                    removeDeletes: Boolean,
                    minSegmentSize: Long,
                    valuesConfig: ValuesBlock.Config,
                    sortedIndexConfig: SortedIndexBlock.Config,
                    binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                    hashIndexConfig: HashIndexBlock.Config,
                    bloomFilterConfig: BloomFilterBlock.Config)(implicit keyOrder: KeyOrder[Slice[Byte]],
                                                                timeOrder: TimeOrder[Slice[Byte]],
                                                                functionStore: FunctionStore,
                                                                keyValueMemorySweeper: Option[MemorySweeper.KeyValue],
                                                                fileSweeper: FileSweeper.Enabled,
                                                                blockCache: Option[BlockCache.State],
                                                                segmentIO: SegmentIO): Slice[Segment] = {

    val splits =
      SegmentMerger.split(
        keyValues = keyValues,
        minSegmentSize = minSegmentSize,
        isLastLevel = removeDeletes,
        forInMemory = false,
        valuesConfig = valuesConfig,
        createdInLevel = createdInLevel,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig
      )

    splits.mapRecover(
      block =
        keyValues => {
          val (segmentId, path) = fetchNextPath
          Segment.persistent(
            path = path,
            segmentId = segmentId,
            createdInLevel = createdInLevel,
            segmentConfig = segmentConfig,
            mmapReads = mmapSegmentsOnRead,
            mmapWrites = mmapSegmentsOnWrite,
            keyValues = keyValues
          )
        },

      recover =
        (segments: Slice[Segment], _: Throwable) =>
          segments foreach {
            segmentToDelete =>
              IO(segmentToDelete.delete) onLeftSideEffect {
                exception =>
                  logger.error(s"Failed to delete Segment '{}' in recover due to failed copyToPersist", segmentToDelete.path, exception)
              }
          }
    )
  }

  def copyToMemory(segment: Segment,
                   createdInLevel: Int,
                   fetchNextPath: => (Long, Path),
                   removeDeletes: Boolean,
                   minSegmentSize: Long,
                   valuesConfig: ValuesBlock.Config,
                   sortedIndexConfig: SortedIndexBlock.Config,
                   binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                   hashIndexConfig: HashIndexBlock.Config,
                   bloomFilterConfig: BloomFilterBlock.Config)(implicit keyOrder: KeyOrder[Slice[Byte]],
                                                               timeOrder: TimeOrder[Slice[Byte]],
                                                               functionStore: FunctionStore,
                                                               fileSweeper: FileSweeper.Enabled,
                                                               keyValueMemorySweeper: Option[MemorySweeper.KeyValue],
                                                               segmentIO: SegmentIO): Slice[Segment] =
    copyToMemory(
      keyValues = segment.getAll(),
      fetchNextPath = fetchNextPath,
      removeDeletes = removeDeletes,
      createdInLevel = createdInLevel,
      minSegmentSize = minSegmentSize,
      valuesConfig = valuesConfig,
      sortedIndexConfig = sortedIndexConfig,
      binarySearchIndexConfig = binarySearchIndexConfig,
      hashIndexConfig = hashIndexConfig,
      bloomFilterConfig = bloomFilterConfig
    )

  def copyToMemory(keyValues: Iterable[KeyValue.ReadOnly],
                   fetchNextPath: => (Long, Path),
                   removeDeletes: Boolean,
                   minSegmentSize: Long,
                   createdInLevel: Int,
                   valuesConfig: ValuesBlock.Config,
                   sortedIndexConfig: SortedIndexBlock.Config,
                   binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                   hashIndexConfig: HashIndexBlock.Config,
                   bloomFilterConfig: BloomFilterBlock.Config)(implicit keyOrder: KeyOrder[Slice[Byte]],
                                                               timeOrder: TimeOrder[Slice[Byte]],
                                                               functionStore: FunctionStore,
                                                               fileSweeper: FileSweeper.Enabled,
                                                               keyValueMemorySweeper: Option[MemorySweeper.KeyValue],
                                                               segmentIO: SegmentIO): Slice[Segment] = {
    val splits =
      SegmentMerger.split(
        keyValues = keyValues,
        minSegmentSize = minSegmentSize,
        isLastLevel = removeDeletes,
        forInMemory = true,
        valuesConfig = valuesConfig,
        createdInLevel = createdInLevel,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig
      )

    //recovery not required. On failure, uncommitted Segments will be GC'd as nothing holds references to them.
    val segments = Slice.create[Segment](splits.size)

    splits foreach {
      split =>
        val (segmentId, path) = fetchNextPath
        segments add
          Segment.memory(
            path = path,
            segmentId = segmentId,
            createdInLevel = createdInLevel,
            keyValues = split
          )
    }

    segments
  }

  def apply(path: Path,
            segmentId: Long,
            blockCacheFileId: Long,
            mmapReads: Boolean,
            mmapWrites: Boolean,
            minKey: Slice[Byte],
            maxKey: MaxKey[Slice[Byte]],
            segmentSize: Int,
            minMaxFunctionId: Option[MinMax[Slice[Byte]]],
            nearestExpiryDeadline: Option[Deadline],
            checkExists: Boolean = true)(implicit keyOrder: KeyOrder[Slice[Byte]],
                                         timeOrder: TimeOrder[Slice[Byte]],
                                         functionStore: FunctionStore,
                                         keyValueMemorySweeper: Option[MemorySweeper.KeyValue],
                                         fileSweeper: FileSweeper.Enabled,
                                         blockCache: Option[BlockCache.State],
                                         segmentIO: SegmentIO): Segment = {

    val file =
      if (mmapReads)
        DBFile.mmapRead(
          path = path,
          ioStrategy = segmentIO.segmentBlockIO(IOAction.OpenResource),
          blockCacheFileId = blockCacheFileId,
          autoClose = true,
          checkExists = checkExists
        )
      else
        DBFile.channelRead(
          path = path,
          ioStrategy = segmentIO.segmentBlockIO(IOAction.OpenResource),
          blockCacheFileId = blockCacheFileId,
          autoClose = true,
          checkExists = checkExists
        )

    PersistentSegment(
      file = file,
      segmentId = segmentId,
      mmapReads = mmapReads,
      mmapWrites = mmapWrites,
      minKey = minKey,
      maxKey = maxKey,
      segmentSize = segmentSize,
      minMaxFunctionId = minMaxFunctionId,
      nearestExpiryDeadline = nearestExpiryDeadline
    )
  }

  /**
   * Reads the [[PersistentSegment]] when the min, max keys & fileSize is not known.
   *
   * This function requires the Segment to be opened and read. After the Segment is successfully
   * read the file is closed.
   *
   * This function is only used for Appendix file recovery initialization.
   */
  def apply(path: Path,
            segmentId: Long,
            mmapReads: Boolean,
            mmapWrites: Boolean,
            checkExists: Boolean)(implicit keyOrder: KeyOrder[Slice[Byte]],
                                  timeOrder: TimeOrder[Slice[Byte]],
                                  functionStore: FunctionStore,
                                  blockCache: Option[BlockCache.State],
                                  keyValueMemorySweeper: Option[MemorySweeper.KeyValue],
                                  fileSweeper: FileSweeper.Enabled): Segment = {

    implicit val segmentIO: SegmentIO = SegmentIO.defaultSynchronisedStoredIfCompressed
    implicit val blockCacheMemorySweeper: Option[MemorySweeper.Block] = blockCache.map(_.sweeper)

    val file =
      if (mmapReads)
        DBFile.mmapRead(
          path = path,
          ioStrategy = segmentIO.segmentBlockIO(IOAction.OpenResource),
          blockCacheFileId = BlockCacheFileIDGenerator.nextID,
          autoClose = false,
          checkExists = checkExists
        )
      else
        DBFile.channelRead(
          path = path,
          ioStrategy = segmentIO.segmentBlockIO(IOAction.OpenResource),
          blockCacheFileId = BlockCacheFileIDGenerator.nextID,
          autoClose = false,
          checkExists = checkExists
        )

    val refReader = BlockRefReader(file)

    val segmentBlockCache =
      SegmentBlockCache(
        path = Paths.get("Reading segment"),
        segmentIO = segmentIO,
        blockRef = refReader
      )

    val footer = segmentBlockCache.getFooter()
    val sortedIndexReader = segmentBlockCache.createSortedIndexReader()
    val valuesReader = segmentBlockCache.createValuesReader()

    val keyValues =
      SortedIndexBlock.readAll(
        keyValueCount = footer.keyValueCount,
        sortedIndexReader = sortedIndexReader,
        valuesReader = valuesReader
      )

    file.close()

    val deadlineMinMaxFunctionId = DeadlineAndFunctionId(keyValues)

    PersistentSegment(
      file = file,
      segmentId = segmentId,
      mmapReads = mmapReads,
      mmapWrites = mmapWrites,
      minKey = keyValues.head.key.unslice(),
      maxKey =
        keyValues.last match {
          case fixed: KeyValue.ReadOnly.Fixed =>
            MaxKey.Fixed(fixed.key.unslice())

          case range: KeyValue.ReadOnly.Range =>
            MaxKey.Range(range.fromKey.unslice(), range.toKey.unslice())
        },
      minMaxFunctionId = deadlineMinMaxFunctionId.minMaxFunctionId,
      segmentSize = refReader.offset.size,
      nearestExpiryDeadline = deadlineMinMaxFunctionId.nearestDeadline
    )
  }

  def belongsTo(keyValue: KeyValue,
                segment: Segment)(implicit keyOrder: KeyOrder[Slice[Byte]]): Boolean = {
    import keyOrder._
    keyValue.key >= segment.minKey && {
      if (segment.maxKey.inclusive)
        keyValue.key <= segment.maxKey.maxKey
      else
        keyValue.key < segment.maxKey.maxKey
    }
  }

  def overlaps(minKey: Slice[Byte],
               maxKey: Slice[Byte],
               maxKeyInclusive: Boolean,
               segment: Segment)(implicit keyOrder: KeyOrder[Slice[Byte]]): Boolean =
    Slice.intersects((minKey, maxKey, maxKeyInclusive), (segment.minKey, segment.maxKey.maxKey, segment.maxKey.inclusive))

  def overlaps(minKey: Slice[Byte],
               maxKey: Slice[Byte],
               maxKeyInclusive: Boolean,
               segments: Iterable[Segment])(implicit keyOrder: KeyOrder[Slice[Byte]]): Boolean =
    segments.exists(segment => overlaps(minKey, maxKey, maxKeyInclusive, segment))

  def overlaps(map: Map[Slice[Byte], Memory],
               segments: Iterable[Segment])(implicit keyOrder: KeyOrder[Slice[Byte]]): Boolean =
    Segment.minMaxKey(map) exists {
      case (minKey, maxKey, maxKeyInclusive) =>
        Segment.overlaps(
          minKey = minKey,
          maxKey = maxKey,
          maxKeyInclusive = maxKeyInclusive,
          segments = segments
        )
    }

  def overlaps(segment1: Segment,
               segment2: Segment)(implicit keyOrder: KeyOrder[Slice[Byte]]): Boolean =
    Slice.intersects((segment1.minKey, segment1.maxKey.maxKey, segment1.maxKey.inclusive), (segment2.minKey, segment2.maxKey.maxKey, segment2.maxKey.inclusive))

  def partitionOverlapping(segments1: Iterable[Segment],
                           segments2: Iterable[Segment])(implicit keyOrder: KeyOrder[Slice[Byte]]): (Iterable[Segment], Iterable[Segment]) =
    segments1
      .partition(segmentToWrite => segments2.exists(existingSegment => Segment.overlaps(segmentToWrite, existingSegment)))

  def nonOverlapping(segments1: Iterable[Segment],
                     segments2: Iterable[Segment])(implicit keyOrder: KeyOrder[Slice[Byte]]): Iterable[Segment] =
    nonOverlapping(segments1, segments2, segments1.size)

  def nonOverlapping(segments1: Iterable[Segment],
                     segments2: Iterable[Segment],
                     count: Int)(implicit keyOrder: KeyOrder[Slice[Byte]]): Iterable[Segment] = {
    if (count == 0)
      Iterable.empty
    else {
      val resultSegments = ListBuffer.empty[Segment]
      segments1 foreachBreak {
        segment1 =>
          if (!segments2.exists(segment2 => overlaps(segment1, segment2)))
            resultSegments += segment1
          resultSegments.size == count
      }
      resultSegments
    }
  }

  def overlaps(segments1: Iterable[Segment],
               segments2: Iterable[Segment])(implicit keyOrder: KeyOrder[Slice[Byte]]): Iterable[Segment] =
    segments1.filter(segment1 => segments2.exists(segment2 => overlaps(segment1, segment2)))

  def overlaps(segment: Segment,
               segments2: Iterable[Segment])(implicit keyOrder: KeyOrder[Slice[Byte]]): Boolean =
    segments2.exists(segment2 => overlaps(segment, segment2))

  def intersects(segments1: Iterable[Segment], segments2: Iterable[Segment]): Boolean =
    if (segments1.isEmpty || segments2.isEmpty)
      false
    else
      segments1.exists(segment1 => segments2.exists(_.path == segment1.path))

  def intersects(segment: Segment, segments2: Iterable[Segment]): Boolean =
    segments2.exists(_.path == segment.path)

  /**
   * Pre condition: Segments should be sorted with their minKey in ascending order.
   */
  def getAllKeyValues(segments: Iterable[Segment]): Slice[KeyValue.ReadOnly] =
    if (segments.isEmpty) {
      Slice.empty
    } else if (segments.size == 1) {
      segments.head.getAll()
    } else {
      val totalKeyValues =
        segments.foldLeftRecover(0) {
          case (total, segment) =>
            segment.getKeyValueCount() + total
        }

      segments.foldLeftRecover(Slice.create[KeyValue.ReadOnly](totalKeyValues)) {
        case (allKeyValues, segment) =>
          segment getAll Some(allKeyValues)
      }
    }

  def deleteSegments(segments: Iterable[Segment]): Int =
    segments.foldLeftRecover(0, failFast = false) {
      case (deleteCount, segment) =>
        segment.delete
        deleteCount + 1
    }

  def tempMinMaxKeyValues(segments: Iterable[Segment]): Slice[Memory] =
    segments.foldLeft(Slice.create[Memory](segments.size * 2)) {
      case (keyValues, segment) =>
        keyValues add Memory.Put(segment.minKey, None, None, Time.empty)
        segment.maxKey match {
          case MaxKey.Fixed(maxKey) =>
            keyValues add Memory.Put(maxKey, None, None, Time.empty)

          case MaxKey.Range(fromKey, maxKey) =>
            keyValues add Memory.Range(fromKey, maxKey, None, Value.Update(Some(maxKey), None, Time.empty))
        }
    }

  def tempMinMaxKeyValues(map: Map[Slice[Byte], Memory]): Slice[Memory] = {
    for {
      minKey <- map.skipList.head().map(memory => Memory.Put(memory.key, None, None, Time.empty))
      maxKey <- map.skipList.last() map {
        case fixed: Memory.Fixed =>
          Memory.Put(fixed.key, None, None, Time.empty)

        case Memory.Range(fromKey, toKey, _, _) =>
          Memory.Range(fromKey, toKey, None, Value.Update(None, None, Time.empty))
      }
    } yield
      Slice(minKey, maxKey)
  } getOrElse Slice.create[Memory](0)

  def minMaxKey(map: Map[Slice[Byte], Memory]): Option[(Slice[Byte], Slice[Byte], Boolean)] =
    for {
      minKey <- map.skipList.head().map(_.key)
      maxKey <- map.skipList.last() map {
        case fixed: Memory.Fixed =>
          (fixed.key, true)

        case range: Memory.Range =>
          (range.toKey, false)
      }
    } yield (minKey, maxKey._1, maxKey._2)

  def minMaxKey(segment: Iterable[Segment]): Option[(Slice[Byte], Slice[Byte], Boolean)] =
    for {
      minKey <- segment.headOption.map(_.minKey)
      maxKey <- segment.lastOption.map(_.maxKey) map {
        case MaxKey.Fixed(maxKey) =>
          (maxKey, true)

        case MaxKey.Range(_, maxKey) =>
          (maxKey, false)
      }
    } yield {
      (minKey, maxKey._1, maxKey._2)
    }

  def minMaxKey(left: Iterable[Segment],
                right: Iterable[Segment])(implicit keyOrder: KeyOrder[Slice[Byte]]): Option[(Slice[Byte], Slice[Byte], Boolean)] =
    Slice.minMax(Segment.minMaxKey(left), Segment.minMaxKey(right))

  def minMaxKey(left: Iterable[Segment],
                right: Map[Slice[Byte], Memory])(implicit keyOrder: KeyOrder[Slice[Byte]]): Option[(Slice[Byte], Slice[Byte], Boolean)] =
    Slice.minMax(Segment.minMaxKey(left), Segment.minMaxKey(right))

  def overlapsWithBusySegments(inputSegments: Iterable[Segment],
                               busySegments: Iterable[Segment],
                               appendixSegments: Iterable[Segment])(implicit keyOrder: KeyOrder[Slice[Byte]],
                                                                    segmentIO: SegmentIO): Boolean =
    if (busySegments.isEmpty)
      false
    else {
      val assignments =
        SegmentAssigner.assignMinMaxOnlyUnsafe(
          inputSegments = inputSegments,
          targetSegments = appendixSegments
        )

      Segment.overlaps(
        segments1 = busySegments,
        segments2 = assignments
      ).nonEmpty
    }

  def overlapsWithBusySegments(map: Map[Slice[Byte], Memory],
                               busySegments: Iterable[Segment],
                               appendixSegments: Iterable[Segment])(implicit keyOrder: KeyOrder[Slice[Byte]]): Boolean =
    if (busySegments.isEmpty)
      false
    else {
      for {
        head <- map.skipList.head()
        last <- map.skipList.last()
      } yield {
        val assignments =
          if (keyOrder.equiv(head.key, last.key))
            SegmentAssigner.assignUnsafe(keyValues = Slice(head), segments = appendixSegments)
          else
            SegmentAssigner.assignUnsafe(keyValues = Slice(head, last), segments = appendixSegments)

        Segment.overlaps(
          segments1 = busySegments,
          segments2 = assignments.keys
        ).nonEmpty
      }
    } getOrElse false

  def getNearestDeadline(deadline: Option[Deadline],
                         keyValue: KeyValue): Option[Deadline] =
    keyValue match {
      case readOnly: KeyValue.ReadOnly =>
        getNearestDeadline(deadline, readOnly)

      case writeOnly: Transient =>
        getNearestDeadline(deadline, writeOnly)
    }

  def getNearestDeadline(deadline: Option[Deadline],
                         next: KeyValue.ReadOnly): Option[Deadline] =
    next match {
      case readOnly: KeyValue.ReadOnly.Put =>
        FiniteDurations.getNearestDeadline(deadline, readOnly.deadline)

      case readOnly: KeyValue.ReadOnly.Remove =>
        FiniteDurations.getNearestDeadline(deadline, readOnly.deadline)

      case readOnly: KeyValue.ReadOnly.Update =>
        FiniteDurations.getNearestDeadline(deadline, readOnly.deadline)

      case readOnly: KeyValue.ReadOnly.PendingApply =>
        FiniteDurations.getNearestDeadline(deadline, readOnly.deadline)

      case _: KeyValue.ReadOnly.Function =>
        deadline

      case range: KeyValue.ReadOnly.Range =>
        range.fetchFromAndRangeValueUnsafe match {
          case (Some(fromValue), rangeValue) =>
            val fromValueDeadline = getNearestDeadline(deadline, fromValue)
            getNearestDeadline(fromValueDeadline, rangeValue)

          case (None, rangeValue) =>
            getNearestDeadline(deadline, rangeValue)
        }
    }

  def getNearestDeadline(deadline: Option[Deadline],
                         keyValue: Transient): Option[Deadline] =
    keyValue match {
      case writeOnly: Transient.Fixed =>
        FiniteDurations.getNearestDeadline(deadline, writeOnly.deadline)

      case range: Transient.Range =>
        (range.fromValue, range.rangeValue) match {
          case (Some(fromValue), rangeValue) =>
            val fromValueDeadline = getNearestDeadline(deadline, fromValue)
            getNearestDeadline(fromValueDeadline, rangeValue)

          case (None, rangeValue) =>
            getNearestDeadline(deadline, rangeValue)
        }
    }

  def getNearestDeadline(deadline: Option[Deadline],
                         keyValue: Value.FromValue): Option[Deadline] =
    keyValue match {
      case rangeValue: Value.RangeValue =>
        getNearestDeadline(deadline, rangeValue)

      case put: Value.Put =>
        FiniteDurations.getNearestDeadline(deadline, put.deadline)
    }

  def getNearestDeadline(deadline: Option[Deadline],
                         rangeValue: Value.RangeValue): Option[Deadline] =
    rangeValue match {
      case remove: Value.Remove =>
        FiniteDurations.getNearestDeadline(deadline, remove.deadline)
      case update: Value.Update =>
        FiniteDurations.getNearestDeadline(deadline, update.deadline)
      case _: Value.Function =>
        deadline
      case pendingApply: Value.PendingApply =>
        FiniteDurations.getNearestDeadline(deadline, pendingApply.deadline)
    }

  def getNearestDeadline(previous: Option[Deadline],
                         applies: Slice[Value.Apply]): Option[Deadline] =
    applies.foldLeft(previous) {
      case (deadline, apply) =>
        getNearestDeadline(
          deadline = deadline,
          rangeValue = apply
        )
    }

  def getNearestDeadline(keyValues: Iterable[KeyValue]): Option[Deadline] =
    keyValues.foldLeftRecover(Option.empty[Deadline])(getNearestDeadline)

  def getNearestDeadlineSegment(previous: Segment,
                                next: Segment): Option[Segment] =
    (previous.nearestExpiryDeadline, next.nearestExpiryDeadline) match {
      case (None, None) => None
      case (Some(_), None) => Some(previous)
      case (None, Some(_)) => Some(next)
      case (Some(previousDeadline), Some(nextDeadline)) =>
        if (previousDeadline < nextDeadline)
          Some(previous)
        else
          Some(next)
    }

  def getNearestDeadlineSegment(segments: Iterable[Segment]): Option[Segment] =
    segments.foldLeft(Option.empty[Segment]) {
      case (previous, next) =>
        previous map {
          previous =>
            getNearestDeadlineSegment(previous, next)
        } getOrElse {
          if (next.nearestExpiryDeadline.isDefined)
            Some(next)
          else
            None
        }
    }
}

private[core] trait Segment extends FileSweeperItem {
  val segmentId: Long
  val minKey: Slice[Byte]
  val maxKey: MaxKey[Slice[Byte]]
  val segmentSize: Int
  val nearestExpiryDeadline: Option[Deadline]
  val minMaxFunctionId: Option[MinMax[Slice[Byte]]]

  def createdInLevel: Int

  def path: Path

  def put(newKeyValues: Slice[KeyValue.ReadOnly],
          minSegmentSize: Long,
          removeDeletes: Boolean,
          createdInLevel: Int,
          valuesConfig: ValuesBlock.Config,
          sortedIndexConfig: SortedIndexBlock.Config,
          binarySearchIndexConfig: BinarySearchIndexBlock.Config,
          hashIndexConfig: HashIndexBlock.Config,
          bloomFilterConfig: BloomFilterBlock.Config,
          segmentConfig: SegmentBlock.Config,
          targetPaths: PathsDistributor = PathsDistributor(Seq(Dir(path.getParent, 1)), () => Seq()))(implicit idGenerator: IDGenerator): Slice[Segment]

  def refresh(minSegmentSize: Long,
              removeDeletes: Boolean,
              createdInLevel: Int,
              valuesConfig: ValuesBlock.Config,
              sortedIndexConfig: SortedIndexBlock.Config,
              binarySearchIndexConfig: BinarySearchIndexBlock.Config,
              hashIndexConfig: HashIndexBlock.Config,
              bloomFilterConfig: BloomFilterBlock.Config,
              segmentConfig: SegmentBlock.Config,
              targetPaths: PathsDistributor = PathsDistributor(Seq(Dir(path.getParent, 1)), () => Seq()))(implicit idGenerator: IDGenerator): Slice[Segment]

  def getFromCache(key: Slice[Byte]): Option[KeyValue.ReadOnly]

  def mightContainKey(key: Slice[Byte]): Boolean

  def mightContainFunction(key: Slice[Byte]): Boolean

  def get(key: Slice[Byte], readState: ReadState): Option[KeyValue.ReadOnly]

  def lower(key: Slice[Byte], readState: ReadState): Option[KeyValue.ReadOnly]

  def higher(key: Slice[Byte], readState: ReadState): Option[KeyValue.ReadOnly]

  def floorHigherHint(key: Slice[Byte]): Option[Slice[Byte]]

  def getAll(addTo: Option[Slice[KeyValue.ReadOnly]] = None): Slice[KeyValue.ReadOnly]

  def delete: Unit

  def deleteSegmentsEventually: Unit

  def close: Unit

  def getKeyValueCount(): Int

  def clearCachedKeyValues(): Unit

  def clearAllCaches(): Unit

  def isInKeyValueCache(key: Slice[Byte]): Boolean

  def isKeyValueCacheEmpty: Boolean

  def areAllCachesEmpty: Boolean

  def cachedKeyValueSize: Int

  def hasRange: Boolean

  def hasPut: Boolean

  def isFooterDefined: Boolean

  def hasBloomFilter: Boolean

  def isOpen: Boolean

  def isFileDefined: Boolean

  def memory: Boolean

  def persistent: Boolean

  def existsOnDisk: Boolean
}
