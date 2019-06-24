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

package swaydb.core.segment.format.a.index

import swaydb.compression.CompressionInternal
import swaydb.core.CommonAssertions._
import swaydb.core.RunThis._
import swaydb.core.TestBase
import swaydb.core.TestData._
import swaydb.core.data.Transient
import swaydb.core.io.reader.Reader
import swaydb.data.IO
import swaydb.data.order.KeyOrder
import swaydb.data.slice.Slice

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class HashIndexSpec extends TestBase {

  implicit val keyOrder = KeyOrder.default

  val keyValueCount = 10000

  import keyOrder._

  "optimalBytesRequired" should {
    "allocate optimal byte" in {
      HashIndex.optimalBytesRequired(
        keyCounts = 1,
        largestValue = 1,
        compensate = _ => 0
      ) shouldBe
        HashIndex.headerSize(
          keyCounts = 1,
          writeAbleLargestValueSize = 1
        ) + 1 + 1
    }
  }

  "it" should {

    "build index" when {
      "there are only fixed key-values" in {
        runThis(10.times) {
          val maxProbe = 1000
          val startId = Some(0)
          val keyValues =
            randomKeyValues(
              count = 100000,
              startId = startId,
              addRandomRemoves = true,
              addRandomFunctions = true,
              addRandomRemoveDeadlines = true,
              addRandomUpdates = true,
              addRandomPendingApply = true,
              resetPrefixCompressionEvery = randomIntMax(20),
              hashIndexCompensation = size => size * 3
            )

          keyValues should not be empty

          val state =
            HashIndex.init(
              maxProbe = maxProbe,
              keyValues = keyValues
            ).get

          keyValues foreach {
            keyValue =>
              HashIndex.write(
                key = keyValue.key,
                value = keyValue.stats.thisKeyValuesAccessIndexOffset,
                state = state
              ) map {
                _ =>
                  state
              }
          }

          println(s"hit: ${state.hit}")
          println(s"miss: ${state.miss}")
          println

          HashIndex.writeHeader(state).get

          println(s"Bytes allocated: ${state.bytes.size}")
          println(s"Bytes written: ${state.bytes.written}")
          println("Compressed bytes: " + CompressionInternal.randomLZ4().compressor.compress(state.bytes.unslice()).get.get.size)

          state.hit should be(keyValues.size)
          state.miss shouldBe 0
          state.hit + state.miss shouldBe keyValues.size

          val offset = HashIndex.Offset(0, state.bytes.written)
          val hashIndex = HashIndex.read(offset, Reader(state.bytes)).get

          hashIndex shouldBe
            HashIndex(
              offset = offset,
              formatId = HashIndex.formatID,
              maxProbe = state.maxProbe,
              hit = state.hit,
              miss = state.miss,
              writeAbleLargestValueSize = state.writeAbleLargestValueSize,
              headerSize = HashIndex.headerSize(keyValues.last.stats.segmentUniqueKeysCount, state.writeAbleLargestValueSize),
              allocatedBytes = state.bytes.underlyingArraySize
            )

          println("Building ListMap")
          val indexOffsetMap = mutable.HashMap.empty[Int, ListBuffer[Transient]]

          keyValues foreach {
            keyValue =>
              indexOffsetMap.getOrElseUpdate(keyValue.stats.thisKeyValuesAccessIndexOffset, ListBuffer(keyValue)) += keyValue
          }

          println(s"ListMap created with size: ${indexOffsetMap.size}")

          val randomBytes = randomBytesSlice(randomIntMax(100))

          val (adjustedOffset, alteredBytes) =
            eitherOne(
              (offset, state.bytes),
              (offset, state.bytes ++ randomBytesSlice(randomIntMax(100))),
              (offset.copy(start = randomBytes.size), randomBytes ++ state.bytes),
              (offset.copy(start = randomBytes.size), randomBytes ++ state.bytes ++ randomBytesSlice(randomIntMax(100)))
            )

          def findKey(indexOffset: Int, key: Slice[Byte]): IO[Option[Transient]] =
            indexOffsetMap.get(indexOffset) match {
              case Some(keyValues) =>
                IO(keyValues.find(_.key equiv key))

              case None =>
                IO.Failure(IO.Error.Fatal(s"Reading index that does not exist: $indexOffset"))
            }

          keyValues foreach {
            keyValue =>
              val found =
                HashIndex.find(
                  key = keyValue.key,
                  offset = adjustedOffset,
                  reader = Reader(alteredBytes),
                  hashIndex = hashIndex,
                  assertValue = findKey(_, keyValue.key)
                ).get.get

              (found.key equiv keyValue.key) shouldBe true
          }
        }
      }
    }
  }
}
