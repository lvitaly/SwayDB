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

package swaydb.core.segment.format.a.entry

import org.scalatest.WordSpec
import swaydb.IOValues._
import swaydb.core.CommonAssertions._
import swaydb.core.RunThis._
import swaydb.core.TestData._
import swaydb.core.TestTimer
import swaydb.core.segment.format.a.entry.reader.EntryReader
import swaydb.data.order.KeyOrder
import swaydb.data.slice.Slice

class GroupEntryReaderWriterSpec extends WordSpec {

  implicit val keyOrder: KeyOrder[Slice[Byte]] = KeyOrder.default

  "write and read single Group entry" in {
    runThis(100.times, log = true) {
      implicit val testTimer = TestTimer.random
      val entry = randomGroup(keyValues = randomizedKeyValues(count = 10, addPut = true))
      //      println("write: " + entry)

      val read =
        EntryReader.read(
          indexEntry = entry.indexEntryBytes.dropIntUnsigned().value,
          mightBeCompressed = entry.stats.hasPrefixCompression,
          valueCache = Some(buildSingleValueCache(entry.valueEntryBytes.flatten.toSlice)),
          indexOffset = 0,
          nextIndexOffset = 0,
          nextIndexSize = 0,
          isNormalised = false,
          hasAccessPositionIndex = entry.sortedIndexConfig.enableAccessPositionIndex,
          previous = None
        ).runRandomIO.value

      //      println("read:  " + read)
      read shouldBe entry
    }
  }

  "write and read Group entries with other entries" in {
    runThis(1000.times, log = true) {
      implicit val testTimer = TestTimer.random

      val keyValues = randomizedKeyValues(count = 1, addPut = true)
      val previous = keyValues.head

      val next = randomGroup(randomizedKeyValues(count = 100, addPut = true), previous = Some(previous))

      //      println("previous: " + previous)
      //      println("next: " + next)

      val valueBytes = Slice((previous.valueEntryBytes ++ next.valueEntryBytes).flatten.toArray)

      val previousRead =
        EntryReader.read(
          indexEntry = previous.indexEntryBytes.dropIntUnsigned().value,
          mightBeCompressed = false,
          valueCache = Some(buildSingleValueCache(valueBytes)),
          indexOffset = 0,
          nextIndexOffset = 0,
          nextIndexSize = 0,
          isNormalised = false,
          hasAccessPositionIndex = previous.sortedIndexConfig.enableAccessPositionIndex,
          previous = None
        ).runRandomIO.value

      //      val previousRead = EntryReader.read(Reader(previous.indexEntryBytes), Reader(valueBytes), 0, 0, 0, None).runIO
      previousRead shouldBe previous

      //      val read = EntryReader.read(Reader(next.indexEntryBytes), Reader(valueBytes), 0, 0, 0, Some(previousRead)).runIO
      val nextRead =
        EntryReader.read(
          indexEntry = next.indexEntryBytes.dropIntUnsigned().value,
          mightBeCompressed = next.stats.hasPrefixCompression,
          valueCache = Some(buildSingleValueCache(valueBytes)),
          indexOffset = 0,
          nextIndexOffset = 0,
          nextIndexSize = 0,
          isNormalised = false,
          hasAccessPositionIndex = next.sortedIndexConfig.enableAccessPositionIndex,
          previous = Some(previousRead)
        ).runRandomIO.value
      //      println("read:  " + read)
      //      println
      nextRead shouldBe next
    }
  }
}
