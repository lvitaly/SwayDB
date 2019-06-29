///*
// * Copyright (c) 2019 Simer Plaha (@simerplaha)
// *
// * This file is a part of SwayDB.
// *
// * SwayDB is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as
// * published by the Free Software Foundation, either version 3 of the
// * License, or (at your option) any later version.
// *
// * SwayDB is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with SwayDB. If not, see <https://www.gnu.org/licenses/>.
// */
//
//package swaydb.core.segment.format.a.block
//
//import swaydb.core.CommonAssertions._
//import swaydb.core.RunThis._
//import swaydb.core.TestBase
//import swaydb.core.TestData._
//import swaydb.core.data.Transient
//import swaydb.core.io.reader.Reader
//import swaydb.core.segment.format.a.{KeyMatcher, SegmentWriter}
//import swaydb.data.IO
//import swaydb.data.order.KeyOrder
//import swaydb.data.slice.Slice
//
//import scala.collection.mutable
//import scala.collection.mutable.ListBuffer
//import scala.util.Random
//
//class HashIndexSpec extends TestBase {
//
//  implicit val keyOrder = KeyOrder.default
//
//  val keyValueCount = 10000
//
//  import keyOrder._
//
//  "optimalBytesRequired" should {
//    "allocate optimal byte" in {
//      HashIndex.optimalBytesRequired(
//        keyCounts = 1,
//        largestValue = 1,
//        allocateSpace = _ => 0,
//        hasCompression = false
//      ) shouldBe
//        HashIndex.headerSize(
//          keyCounts = 1,
//          hasCompression = false,
//          writeAbleLargestValueSize = 1
//        ) + 1 + 1
//    }
//  }
//
//  "it" should {
//    "write compressed HashIndex and result in the same as uncompressed HashIndex" in {
//      runThis(100.times) {
//        val maxProbe = 10
//        val keyValues =
//          randomKeyValues(
//            count = 1000,
//            addRandomRemoves = true,
//            addRandomFunctions = true,
//            addRandomRemoveDeadlines = true,
//            addRandomUpdates = true,
//            addRandomPendingApply = true,
//            resetPrefixCompressionEvery = randomIntMax(10),
//            allocateSpace = _.requiredSpace * randomIntMax(2)
//          )
//
//        keyValues should not be empty
//
//        val uncompressedState =
//          HashIndex.init(
//            maxProbe = maxProbe,
//            keyValues = keyValues,
//            compressions = Seq.empty
//          ).get
//
//        val compressedState =
//          HashIndex.init(
//            maxProbe = maxProbe,
//            keyValues = keyValues,
//            compressions = Seq(randomCompression())
//          ).get
//
//        keyValues foreach {
//          keyValue =>
//            HashIndex.write(
//              key = keyValue.key,
//              value = keyValue.stats.thisKeyValuesAccessIndexOffset,
//              state = uncompressedState
//            ).get
//
//            HashIndex.write(
//              key = keyValue.key,
//              value = keyValue.stats.thisKeyValuesAccessIndexOffset,
//              state = compressedState
//            ).get
//        }
//
//        HashIndex.close(uncompressedState).get
//        HashIndex.close(compressedState).get
//
//        //compressed bytes should be smaller
//        compressedState.bytes.written should be <= uncompressedState.bytes.written
//
//        val uncompressedOffset = HashIndex.Offset(0, uncompressedState.bytes.size)
//        val compressedOffset = HashIndex.Offset(0, compressedState.bytes.size)
//
//        val uncompressedHashIndex = HashIndex.read(uncompressedOffset, Reader(uncompressedState.bytes)).get
//        val compressedHashIndex = HashIndex.read(compressedOffset, Reader(compressedState.bytes)).get
//
//        uncompressedHashIndex.compressionInfo shouldBe empty
//        compressedHashIndex.compressionInfo shouldBe defined
//
//        uncompressedHashIndex.headerSize shouldBe compressedHashIndex.headerSize
//        uncompressedHashIndex.allocatedBytes shouldBe compressedHashIndex.allocatedBytes
//        uncompressedHashIndex.bytesToReadPerIndex shouldBe compressedHashIndex.bytesToReadPerIndex
//        uncompressedHashIndex.hit shouldBe compressedHashIndex.hit
//        uncompressedHashIndex.miss shouldBe compressedHashIndex.miss
//        uncompressedHashIndex.maxProbe shouldBe compressedHashIndex.maxProbe
//        uncompressedHashIndex.writeAbleLargestValueSize shouldBe compressedHashIndex.writeAbleLargestValueSize
//        uncompressedHashIndex.blockOffset.start shouldBe compressedHashIndex.blockOffset.start
//        uncompressedHashIndex.blockOffset.size should be >= compressedHashIndex.blockOffset.size
//
//        val blockDecompressor = compressedHashIndex.compressionInfo.get
//        blockDecompressor.decompressedLength shouldBe uncompressedState.bytes.written - uncompressedState.headerSize
//        blockDecompressor.decompressedBytes shouldBe empty
//        blockDecompressor.isBusy shouldBe false
//
//        val decompressedBytes =
//          Block.decompress(
//            compressionInfo = compressedHashIndex.compressionInfo.get,
//            segmentReader = Reader(compressedState.bytes),
//            offset = compressedOffset
//          ).get
//
//        val expectedDecompressedBytes = uncompressedState.bytes.drop(uncompressedState.headerSize)
//
//        decompressedBytes shouldBe expectedDecompressedBytes
//      }
//    }
//
//    "build index" when {
//      "the hash is perfect" in {
//        runThis(100.times) {
//          val maxProbe = 100
//          val startId = Some(0)
//          val keyValues =
//            randomKeyValues(
//              count = 1000,
//              startId = startId,
//              addRandomRemoves = true,
//              addRandomFunctions = true,
//              addRandomRemoveDeadlines = true,
//              addRandomUpdates = true,
//              addRandomPendingApply = true,
//              resetPrefixCompressionEvery = 0,
//              allocateSpace = _.requiredSpace * 3 //give it enough space to create a perfect hash.
//            )
//
//          keyValues should not be empty
//
//          val state =
//            HashIndex.init(
//              maxProbe = maxProbe,
//              keyValues = keyValues,
//              compressions = eitherOne(Seq.empty, Seq(randomCompression()))
//            ).get
//
//          val allocatedBytes = state.bytes.size
//
//          keyValues foreach {
//            keyValue =>
//              HashIndex.write(
//                key = keyValue.key,
//                value = keyValue.stats.thisKeyValuesAccessIndexOffset,
//                state = state
//              ).get
//          }
//
//          println(s"hit: ${state.hit}")
//          println(s"miss: ${state.miss}")
//          println
//
//          HashIndex.close(state).get
//
//          println(s"Bytes allocated: ${state.bytes.size}")
//          println(s"Bytes written: ${state.bytes.written}")
//
//          state.hit should be(keyValues.size)
//          state.miss shouldBe 0
//          state.hit + state.miss shouldBe keyValues.size
//
//          val offset = HashIndex.Offset(0, state.bytes.written)
//
//          val randomBytes = randomBytesSlice(randomIntMax(100))
//
//          val (adjustedOffset, alteredBytes) =
//            eitherOne(
//              (offset, state.bytes),
//              (offset, state.bytes ++ randomBytesSlice(randomIntMax(100))),
//              (offset.copy(start = randomBytes.size), randomBytes ++ state.bytes),
//              (offset.copy(start = randomBytes.size), randomBytes ++ state.bytes ++ randomBytesSlice(randomIntMax(100)))
//            )
//
//          val hashIndex = HashIndex.read(adjustedOffset, Reader(alteredBytes)).get
//
//          hashIndex shouldBe
//            HashIndex(
//              blockOffset = adjustedOffset,
//              compressionInfo = hashIndex.compressionInfo,
//              maxProbe = state.maxProbe,
//              hit = state.hit,
//              miss = state.miss,
//              writeAbleLargestValueSize = state.writeAbleLargestValueSize,
//              headerSize = HashIndex.headerSize(keyValues.last.stats.segmentUniqueKeysCount, state.writeAbleLargestValueSize),
//              allocatedBytes = allocatedBytes
//            )
//
//          println("Building ListMap")
//          val indexOffsetMap = mutable.HashMap.empty[Int, ListBuffer[Transient]]
//
//          keyValues foreach {
//            keyValue =>
//              indexOffsetMap.getOrElseUpdate(keyValue.stats.thisKeyValuesAccessIndexOffset, ListBuffer(keyValue)) += keyValue
//          }
//
//          println(s"ListMap created with size: ${indexOffsetMap.size}")
//
//          def findKey(indexOffset: Int, key: Slice[Byte]): IO[Option[Transient]] =
//            indexOffsetMap.get(indexOffset) match {
//              case Some(keyValues) =>
//                IO(keyValues.find(_.key equiv key))
//
//              case None =>
//                IO.Failure(IO.Error.Fatal(s"Got index that does not exist: $indexOffset"))
//            }
//
//          keyValues foreach {
//            keyValue =>
//              val found =
//                HashIndex.find(
//                  key = keyValue.key,
//                  blockReader = hashIndex.createBlockReader(alteredBytes),
//                  assertValue = findKey(_, keyValue.key)
//                ).get.get
//              (found.key equiv keyValue.key) shouldBe true
//          }
//        }
//      }
//    }
//
//    "searching a segment" should {
//      "get" in {
//        val keyValues =
//          randomizedKeyValues(
//            count = 100,
//            startId = Some(1),
//            addRandomGroups = false,
//            compressDuplicateValues = randomBoolean(),
//            enableBinarySearchIndex = false,
//            addRandomRangeRemoves = false,
//            addRandomRanges = false,
//            buildFullBinarySearchIndex = true,
//            resetPrefixCompressionEvery = 2,
//            allocateSpace = _.requiredSpace * 3
//          )
//
//        val segment = SegmentWriter.write(keyValues, segmentCompression = randomSegmentCompression(), 0, 5).get.flattenSegmentBytes
//        val indexes = getIndexes(Reader(segment)).get
//
//        indexes._4.get.block.miss shouldBe 0
//
//        Random.shuffle(keyValues) foreach {
//          keyValue =>
//            indexes._4 shouldBe defined
//            val got = HashIndex.get(KeyMatcher.Get.WhilePrefixCompressed(keyValue.key), indexes._4.get, indexes._3, indexes._2).get.get
//            got shouldBe keyValue
//        }
//      }
//    }
//  }
//}
