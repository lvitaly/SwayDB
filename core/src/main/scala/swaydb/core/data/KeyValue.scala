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

package swaydb.core.data

import swaydb.IO
import swaydb.core.cache.{Cache, CacheNoIO}
import swaydb.core.data.KeyValue.ReadOnly
import swaydb.core.map.serializer.{RangeValueSerializer, ValueSerializer}
import swaydb.core.segment.Segment
import swaydb.core.segment.format.a.block._
import swaydb.core.segment.format.a.block.binarysearch.BinarySearchIndexBlock
import swaydb.core.segment.format.a.block.hashindex.HashIndexBlock
import swaydb.core.segment.format.a.block.reader.UnblockedReader
import swaydb.core.segment.format.a.entry.writer._
import swaydb.core.util.Bytes
import swaydb.data.MaxKey
import swaydb.data.order.KeyOrder
import swaydb.data.slice.Slice
import swaydb.data.util.Maybe.Maybe

import scala.concurrent.duration.{Deadline, FiniteDuration}

private[core] sealed trait KeyValue {
  def key: Slice[Byte]

  def keyLength =
    key.size
}

private[core] object KeyValue {

  /**
   * Read-only instances are only created for Key-values read from disk for Persistent Segments
   * and are stored in-memory after merge for Memory Segments.
   */
  sealed trait ReadOnly extends KeyValue {
    def indexEntryDeadline: Option[Deadline]
  }

  /**
   * Key-values that can be added to [[MemorySweeper]].
   *
   * These key-values can remain in memory depending on the cacheSize and are dropped or uncompressed on overflow.
   */
  sealed trait CacheAble extends ReadOnly {
    def valueLength: Int
  }

  object ReadOnly {
    /**
     * An API response type expected from a [[swaydb.core.map.Map]] or [[swaydb.core.segment.Segment]].
     */
    sealed trait Fixed extends KeyValue with ReadOnly {
      def toFromValue(): Value.FromValue

      def toRangeValue(): Value.RangeValue

      def time: Time
    }

    sealed trait Put extends KeyValue.ReadOnly.Fixed {
      def valueLength: Int
      def deadline: Option[Deadline]
      def hasTimeLeft(): Boolean
      def isOverdue(): Boolean = !hasTimeLeft()
      def hasTimeLeftAtLeast(minus: FiniteDuration): Boolean
      def getOrFetchValue: Option[Slice[Byte]]
      def time: Time
      def toFromValue(): Value.Put
      def copyWithDeadlineAndTime(deadline: Option[Deadline], time: Time): KeyValue.ReadOnly.Put
      def copyWithTime(time: Time): KeyValue.ReadOnly.Put
    }

    sealed trait Remove extends KeyValue.ReadOnly.Fixed {
      def deadline: Option[Deadline]
      def hasTimeLeft(): Boolean
      def isOverdue(): Boolean = !hasTimeLeft()
      def hasTimeLeftAtLeast(minus: FiniteDuration): Boolean
      def time: Time
      def toFromValue(): Value.Remove
      def toRemoveValue(): Value.Remove
      def copyWithTime(time: Time): KeyValue.ReadOnly.Remove
    }

    sealed trait Update extends KeyValue.ReadOnly.Fixed {
      def deadline: Option[Deadline]
      def hasTimeLeft(): Boolean
      def isOverdue(): Boolean = !hasTimeLeft()
      def hasTimeLeftAtLeast(minus: FiniteDuration): Boolean
      def time: Time
      def getOrFetchValue: Option[Slice[Byte]]
      def toFromValue(): Value.Update
      def toPut(): KeyValue.ReadOnly.Put
      def toPut(deadline: Option[Deadline]): KeyValue.ReadOnly.Put
      def copyWithDeadlineAndTime(deadline: Option[Deadline], time: Time): KeyValue.ReadOnly.Update
      def copyWithDeadline(deadline: Option[Deadline]): KeyValue.ReadOnly.Update
      def copyWithTime(time: Time): KeyValue.ReadOnly.Update
    }

    sealed trait Function extends KeyValue.ReadOnly.Fixed {
      def time: Time
      def getOrFetchFunction: Slice[Byte]
      def toFromValue(): Value.Function
      def copyWithTime(time: Time): Function
    }

    sealed trait PendingApply extends KeyValue.ReadOnly.Fixed {
      def getOrFetchApplies: Slice[Value.Apply]
      def toFromValue(): Value.PendingApply
      def time: Time
      def deadline: Option[Deadline]
    }

    object Range {
      implicit class RangeImplicit(range: KeyValue.ReadOnly.Range) {
        @inline def contains(key: Slice[Byte])(implicit keyOrder: KeyOrder[Slice[Byte]]): Boolean = {
          import keyOrder._
          key >= range.fromKey && key < range.toKey
        }

        @inline def containsLower(key: Slice[Byte])(implicit keyOrder: KeyOrder[Slice[Byte]]): Boolean = {
          import keyOrder._
          key > range.fromKey && key <= range.toKey
        }
      }
    }

    sealed trait Range extends KeyValue.ReadOnly {
      def fromKey: Slice[Byte]
      def toKey: Slice[Byte]
      def fetchFromValueUnsafe: Option[Value.FromValue]
      def fetchRangeValueUnsafe: Value.RangeValue
      def fetchFromAndRangeValueUnsafe: (Option[Value.FromValue], Value.RangeValue)
      def fetchFromOrElseRangeValueUnsafe: Value.FromValue = {
        val (fromValue, rangeValue) = fetchFromAndRangeValueUnsafe
        fromValue getOrElse rangeValue
      }
    }
  }

  type KeyValueTuple = (Slice[Byte], Option[Slice[Byte]])
}

private[swaydb] sealed trait Memory extends KeyValue.ReadOnly

private[swaydb] object Memory {

  sealed trait Fixed extends Memory with KeyValue.ReadOnly.Fixed

  case class Put(key: Slice[Byte],
                 value: Option[Slice[Byte]],
                 deadline: Option[Deadline],
                 time: Time) extends Memory.Fixed with KeyValue.ReadOnly.Put {

    override def indexEntryDeadline: Option[Deadline] = deadline

    override def valueLength: Int =
      value.map(_.size).getOrElse(0)

    def hasTimeLeft(): Boolean =
      deadline.forall(_.hasTimeLeft())

    def hasTimeLeftAtLeast(minus: FiniteDuration): Boolean =
      deadline.forall(deadline => (deadline - minus).hasTimeLeft())

    override def getOrFetchValue: Option[Slice[Byte]] =
      value

    override def toFromValue(): Value.Put =
      Value.Put(value, deadline, time)

    override def copyWithDeadlineAndTime(deadline: Option[Deadline],
                                         time: Time): Put =
      copy(deadline = deadline, time = time)

    override def copyWithTime(time: Time): Put =
      copy(time = time)

    //to do - make type-safe.
    override def toRangeValue(): Value.RangeValue =
      throw IO.throwable("Put cannot be converted to RangeValue")
  }

  case class Update(key: Slice[Byte],
                    value: Option[Slice[Byte]],
                    deadline: Option[Deadline],
                    time: Time) extends KeyValue.ReadOnly.Update with Memory.Fixed {

    override def indexEntryDeadline: Option[Deadline] = deadline

    def hasTimeLeft(): Boolean =
      deadline.forall(_.hasTimeLeft())

    def hasTimeLeftAtLeast(minus: FiniteDuration): Boolean =
      deadline.forall(deadline => (deadline - minus).hasTimeLeft())

    override def getOrFetchValue: Option[Slice[Byte]] =
      value

    override def toFromValue(): Value.Update =
      Value.Update(value, deadline, time)

    override def copyWithDeadlineAndTime(deadline: Option[Deadline],
                                         time: Time): Update =
      copy(deadline = deadline, time = time)

    override def copyWithTime(time: Time): Update =
      copy(time = time)

    override def copyWithDeadline(deadline: Option[Deadline]): Update =
      copy(deadline = deadline)

    override def toPut(): Memory.Put =
      Memory.Put(
        key = key,
        value = value,
        deadline = deadline,
        time = time
      )

    override def toPut(deadline: Option[Deadline]): Memory.Put =
      Memory.Put(
        key = key,
        value = value,
        deadline = deadline,
        time = time
      )

    override def toRangeValue(): Value.Update =
      toFromValue()
  }

  case class Function(key: Slice[Byte],
                      function: Slice[Byte],
                      time: Time) extends KeyValue.ReadOnly.Function with Memory.Fixed {

    override def indexEntryDeadline: Option[Deadline] = None

    override def getOrFetchFunction: Slice[Byte] =
      function

    override def toFromValue(): Value.Function =
      Value.Function(function, time)

    override def copyWithTime(time: Time): Function =
      copy(time = time)

    override def toRangeValue(): Value.Function =
      toFromValue()
  }

  case class PendingApply(key: Slice[Byte],
                          applies: Slice[Value.Apply]) extends KeyValue.ReadOnly.PendingApply with Memory.Fixed {

    override val deadline =
      Segment.getNearestDeadline(None, applies)

    override def indexEntryDeadline: Option[Deadline] = deadline

    def time = Time.fromApplies(applies)

    override def getOrFetchApplies: Slice[Value.Apply] =
      applies

    override def toFromValue(): Value.PendingApply =
      Value.PendingApply(applies)

    override def toRangeValue(): Value.PendingApply =
      toFromValue()
  }

  case class Remove(key: Slice[Byte],
                    deadline: Option[Deadline],
                    time: Time) extends Memory.Fixed with KeyValue.ReadOnly.Remove {

    override def indexEntryDeadline: Option[Deadline] = deadline

    def hasTimeLeft(): Boolean =
      deadline.exists(_.hasTimeLeft())

    def hasTimeLeftAtLeast(atLeast: FiniteDuration): Boolean =
      deadline.exists(deadline => (deadline - atLeast).hasTimeLeft())

    def toRemoveValue(): Value.Remove =
      Value.Remove(deadline, time)

    override def copyWithTime(time: Time): ReadOnly.Remove =
      copy(time = time)

    override def toFromValue(): Value.Remove =
      toRemoveValue()

    override def toRangeValue(): Value.Remove =
      toFromValue()
  }

  object Range {
    def apply(fromKey: Slice[Byte],
              toKey: Slice[Byte],
              fromValue: Value.FromValue,
              rangeValue: Value.RangeValue): Range =
      new Range(fromKey, toKey, Some(fromValue), rangeValue)
  }

  case class Range(fromKey: Slice[Byte],
                   toKey: Slice[Byte],
                   fromValue: Option[Value.FromValue],
                   rangeValue: Value.RangeValue) extends Memory with KeyValue.ReadOnly.Range {

    override def key: Slice[Byte] = fromKey

    override def indexEntryDeadline: Option[Deadline] = None

    override def fetchFromValueUnsafe: Option[Value.FromValue] =
      fromValue

    override def fetchRangeValueUnsafe: Value.RangeValue =
      rangeValue

    override def fetchFromAndRangeValueUnsafe: (Option[Value.FromValue], Value.RangeValue) =
      (fromValue, rangeValue)
  }
}

private[core] sealed trait Transient extends KeyValue { self =>
  val id: Byte
  val isRemoveRangeMayBe: Boolean
  val isRange: Boolean
  val previous: Option[Transient]
  val thisKeyValueAccessIndexPosition: Int

  def mergedKey: Slice[Byte]
  def value: Option[Slice[Byte]]
  def valuesConfig: ValuesBlock.Config
  def sortedIndexConfig: SortedIndexBlock.Config
  def binarySearchIndexConfig: BinarySearchIndexBlock.Config
  def hashIndexConfig: HashIndexBlock.Config
  def bloomFilterConfig: BloomFilterBlock.Config
  def isPrefixCompressed: Boolean
  def stats: Stats
  def deadline: Option[Deadline]
  def indexEntryBytes: Slice[Byte]
  def valueEntryBytes: Option[Slice[Byte]]
  //a flag that returns true if valueBytes are created for this or any of it's previous key-values indicating value slice is required.
  def hasValueEntryBytes: Boolean
  //start value offset is carried current value offset position.
  def currentStartValueOffsetPosition: Int
  def currentEndValueOffsetPosition: Int

  def nextStartValueOffsetPosition: Int =
    if (!hasValueEntryBytes && currentEndValueOffsetPosition == 0)
      0
    else
      currentEndValueOffsetPosition + 1

  def updatePrevious(valuesConfig: ValuesBlock.Config,
                     sortedIndexConfig: SortedIndexBlock.Config,
                     binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                     hashIndexConfig: HashIndexBlock.Config,
                     bloomFilterConfig: BloomFilterBlock.Config,
                     previous: Option[Transient]): Transient

  def reverseIterator: Iterator[Transient] =
    new Iterator[Transient] {
      var currentPrevious: Option[Transient] =
        Some(self)

      override def hasNext: Boolean =
        currentPrevious.isDefined

      override def next(): Transient = {
        val next = currentPrevious.get
        currentPrevious = next.previous
        next
      }
    }
}

private[core] object Transient {

  implicit class TransientIterableImplicits(keyValues: Slice[Transient]) {
    def maxKey() =
      keyValues.last match {
        case range: Range =>
          MaxKey.Range(range.fromKey, range.toKey)
        case fixed: Transient =>
          MaxKey.Fixed(fixed.key)
      }

    def minKey: Slice[Byte] =
      keyValues.head.key
  }

  sealed trait Fixed extends Transient {

    def hasTimeLeft(): Boolean
    def isOverdue(): Boolean = !hasTimeLeft()
    def time: Time
  }

  def hasSameValue(left: Transient, right: Transient): Boolean =
    (left, right) match {
      //Remove
      case (left: Transient.Remove, right: Transient.Remove) => true
      case (left: Transient.Remove, right: Transient.Put) => right.value.isEmpty
      case (left: Transient.Remove, right: Transient.Update) => right.value.isEmpty
      case (left: Transient.Remove, right: Transient.Function) => false
      case (left: Transient.Remove, right: Transient.PendingApply) => false
      case (left: Transient.Remove, right: Transient.Range) => false
      //Put
      case (left: Transient.Put, right: Transient.Remove) => left.value.isEmpty
      case (left: Transient.Put, right: Transient.Put) => left.value == right.value
      case (left: Transient.Put, right: Transient.Update) => left.value == right.value
      case (left: Transient.Put, right: Transient.Function) => left.value contains right.function
      case (left: Transient.Put, right: Transient.PendingApply) => false
      case (left: Transient.Put, right: Transient.Range) => false
      //Update
      case (left: Transient.Update, right: Transient.Remove) => left.value.isEmpty
      case (left: Transient.Update, right: Transient.Put) => left.value == right.value
      case (left: Transient.Update, right: Transient.Update) => left.value == right.value
      case (left: Transient.Update, right: Transient.Function) => left.value contains right.function
      case (left: Transient.Update, right: Transient.PendingApply) => false
      case (left: Transient.Update, right: Transient.Range) => false
      //Function
      case (left: Transient.Function, right: Transient.Remove) => false
      case (left: Transient.Function, right: Transient.Put) => right.value contains left.function
      case (left: Transient.Function, right: Transient.Update) => right.value contains left.function
      case (left: Transient.Function, right: Transient.Function) => left.function == right.function
      case (left: Transient.Function, right: Transient.PendingApply) => false
      case (left: Transient.Function, right: Transient.Range) => false
      //PendingApply
      case (left: Transient.PendingApply, right: Transient.Remove) => false
      case (left: Transient.PendingApply, right: Transient.Put) => false
      case (left: Transient.PendingApply, right: Transient.Update) => false
      case (left: Transient.PendingApply, right: Transient.Function) => false
      case (left: Transient.PendingApply, right: Transient.PendingApply) => left.applies == right.applies
      case (left: Transient.PendingApply, right: Transient.Range) => false
      //Range
      case (left: Transient.Range, right: Transient.Remove) => false
      case (left: Transient.Range, right: Transient.Put) => false
      case (left: Transient.Range, right: Transient.Update) => false
      case (left: Transient.Range, right: Transient.Function) => false
      case (left: Transient.Range, right: Transient.PendingApply) => false
      case (left: Transient.Range, right: Transient.Range) => left.fromValue == right.fromValue && left.rangeValue == right.rangeValue
    }

  def compressibleValue(keyValue: Transient): Option[Slice[Byte]] =
    keyValue match {
      case transient: Transient =>
        //if value is empty byte slice, return None instead of empty Slice.We do not store empty byte arrays.
        if (transient.value.exists(_.isEmpty))
          None
        else
          transient.value
    }

  def enablePrefixCompression(keyValue: Transient): Boolean =
    keyValue.sortedIndexConfig.prefixCompressionResetCount > 0 &&
      keyValue.previous.exists {
        previous =>
          (previous.stats.linkedPosition + 1) % keyValue.sortedIndexConfig.prefixCompressionResetCount != 0
      }

  def normalise(keyValues: Iterable[Transient]): Slice[Transient] = {
    //Bytes.sizeOf(keyValues.last.stats.segmentMaxSortedIndexEntrySize) is to account for the keySize that gets written to
    //header bytes. This should really be Bytes.sizeOf(keyValues.last.stats.maxKeySize) but that is not calculated which should not
    //make much difference. This is expected to be 1 or two bytes anyway.
    val toSize = Some(keyValues.last.stats.segmentMaxSortedIndexEntrySize + Bytes.sizeOfUnsignedInt(keyValues.last.stats.segmentMaxSortedIndexEntrySize))
    val normalisedKeyValues = Slice.create[Transient](keyValues.size)

    keyValues foreach {
      case keyValue: Transient.Remove =>
        normalisedKeyValues add keyValue.copy(
          normaliseToSize = toSize,
          previous = normalisedKeyValues.lastOption
        )

      case keyValue: Transient.Put =>
        normalisedKeyValues add keyValue.copy(
          normaliseToSize = toSize,
          previous = normalisedKeyValues.lastOption
        )

      case keyValue: Transient.Update =>
        normalisedKeyValues add keyValue.copy(
          normaliseToSize = toSize,
          previous = normalisedKeyValues.lastOption
        )

      case keyValue: Transient.Function =>
        normalisedKeyValues add keyValue.copy(
          normaliseToSize = toSize,
          previous = normalisedKeyValues.lastOption
        )

      case keyValue: Transient.PendingApply =>
        normalisedKeyValues add keyValue.copy(
          normaliseToSize = toSize,
          previous = normalisedKeyValues.lastOption
        )

      case keyValue: Transient.Range =>
        normalisedKeyValues add
          keyValue.copy(
            normaliseToSize = toSize,
            previous = normalisedKeyValues.lastOption
          )
    }

    normalisedKeyValues
  }

  implicit class TransientImplicits(transient: Transient)(implicit keyOrder: KeyOrder[Slice[Byte]]) {

    def toMemoryResponse: Memory =
      transient match {
        case put: Transient.Put =>
          Memory.Put(
            key = put.key,
            value = put.value,
            deadline = put.deadline,
            time = put.time
          )

        case remove: Transient.Remove =>
          Memory.Remove(
            key = remove.key,
            deadline = remove.deadline,
            time = remove.time
          )

        case function: Transient.Function =>
          Memory.Function(
            key = function.key,
            function = function.function,
            time = function.time
          )

        case apply: Transient.PendingApply =>
          Memory.PendingApply(
            key = apply.key,
            applies = apply.applies
          )

        case update: Transient.Update =>
          Memory.Update(
            key = update.key,
            value = update.value,
            deadline = update.deadline,
            time = update.time
          )

        case range: Transient.Range =>
          Memory.Range(
            fromKey = range.fromKey,
            toKey = range.toKey,
            fromValue = range.fromValue,
            rangeValue = range.rangeValue
          )
      }
  }

  object Remove {
    //start with 1 instead of zero to account for HashIndexEntry format entry with non-zero bytes.
    final val id = 1.toByte
  }

  case class Remove(key: Slice[Byte],
                    normaliseToSize: Option[Int],
                    deadline: Option[Deadline],
                    time: Time,
                    valuesConfig: ValuesBlock.Config,
                    sortedIndexConfig: SortedIndexBlock.Config,
                    binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                    hashIndexConfig: HashIndexBlock.Config,
                    bloomFilterConfig: BloomFilterBlock.Config,
                    previous: Option[Transient]) extends Transient.Fixed {
    final val id = Remove.id
    override val isRange: Boolean = false
    override val isRemoveRangeMayBe = false

    override def mergedKey = key

    override def value: Option[Slice[Byte]] = None

    val (indexEntryBytes, valueEntryBytes, currentStartValueOffsetPosition, currentEndValueOffsetPosition, thisKeyValueAccessIndexPosition, keyOffset, isPrefixCompressed) =
      EntryWriter.write(
        current = this,
        currentTime = time,
        normaliseToSize = normaliseToSize,
        compressDuplicateValues = false,
        enablePrefixCompression = Transient.enablePrefixCompression(this)
      ).unapply

    override val hasValueEntryBytes: Boolean = previous.exists(_.hasValueEntryBytes) || valueEntryBytes.exists(_.nonEmpty)

    override val stats =
      Stats(
        unmergedKeySize = key.size,
        mergedKeySize = mergedKey.size,
        keyOffset = keyOffset,
        indexEntry = indexEntryBytes,
        value = valueEntryBytes,
        isRemoveRange = isRemoveRangeMayBe,
        isRange = isRange,
        isPut = false,
        isPrefixCompressed = isPrefixCompressed,
        sortedIndex = sortedIndexConfig,
        bloomFilter = bloomFilterConfig,
        hashIndex = hashIndexConfig,
        binarySearch = binarySearchIndexConfig,
        values = valuesConfig,
        previousStats = previous.map(_.stats),
        deadline = deadline
      )

    override def updatePrevious(valuesConfig: ValuesBlock.Config,
                                sortedIndexConfig: SortedIndexBlock.Config,
                                binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                                hashIndexConfig: HashIndexBlock.Config,
                                bloomFilterConfig: BloomFilterBlock.Config,
                                previous: Option[Transient]): Transient =
      this.copy(
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig,
        previous = previous
      )

    override def hasTimeLeft(): Boolean =
      deadline.exists(_.hasTimeLeft())
  }

  object Put {
    final val id = 2.toByte
  }

  case class Put(key: Slice[Byte],
                 normaliseToSize: Option[Int],
                 value: Option[Slice[Byte]],
                 deadline: Option[Deadline],
                 time: Time,
                 valuesConfig: ValuesBlock.Config,
                 sortedIndexConfig: SortedIndexBlock.Config,
                 binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                 hashIndexConfig: HashIndexBlock.Config,
                 bloomFilterConfig: BloomFilterBlock.Config,
                 previous: Option[Transient]) extends Transient with Transient.Fixed {
    final val id = Put.id
    override val isRemoveRangeMayBe = false
    override val isRange: Boolean = false

    override def mergedKey = key

    val (indexEntryBytes, valueEntryBytes, currentStartValueOffsetPosition, currentEndValueOffsetPosition, thisKeyValueAccessIndexPosition, keyOffset, isPrefixCompressed) =
      EntryWriter.write(
        current = this,
        currentTime = time,
        normaliseToSize = normaliseToSize,
        compressDuplicateValues = valuesConfig.compressDuplicateValues,
        enablePrefixCompression = Transient.enablePrefixCompression(this)
      ).unapply

    override val hasValueEntryBytes: Boolean =
      previous.exists(_.hasValueEntryBytes) || valueEntryBytes.exists(_.nonEmpty)

    val stats =
      Stats(
        unmergedKeySize = key.size,
        mergedKeySize = mergedKey.size,
        keyOffset = keyOffset,
        indexEntry = indexEntryBytes,
        value = valueEntryBytes,
        isRemoveRange = isRemoveRangeMayBe,
        isRange = isRange,
        isPut = true,
        isPrefixCompressed = isPrefixCompressed,
        sortedIndex = sortedIndexConfig,
        bloomFilter = bloomFilterConfig,
        hashIndex = hashIndexConfig,
        binarySearch = binarySearchIndexConfig,
        values = valuesConfig,
        previousStats = previous.map(_.stats),
        deadline = deadline
      )

    override def updatePrevious(valuesConfig: ValuesBlock.Config,
                                sortedIndexConfig: SortedIndexBlock.Config,
                                binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                                hashIndexConfig: HashIndexBlock.Config,
                                bloomFilterConfig: BloomFilterBlock.Config,
                                previous: Option[Transient]): Transient =
      this.copy(
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig,
        previous = previous
      )

    override def hasTimeLeft(): Boolean =
      deadline.forall(_.hasTimeLeft())
  }

  object Update {
    final val id = 3.toByte
  }

  case class Update(key: Slice[Byte],
                    normaliseToSize: Option[Int],
                    value: Option[Slice[Byte]],
                    deadline: Option[Deadline],
                    time: Time,
                    valuesConfig: ValuesBlock.Config,
                    sortedIndexConfig: SortedIndexBlock.Config,
                    binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                    hashIndexConfig: HashIndexBlock.Config,
                    bloomFilterConfig: BloomFilterBlock.Config,
                    previous: Option[Transient]) extends Transient with Transient.Fixed {
    final val id = Update.id
    override val isRemoveRangeMayBe = false
    override val isRange: Boolean = false

    override def mergedKey = key

    val (indexEntryBytes, valueEntryBytes, currentStartValueOffsetPosition, currentEndValueOffsetPosition, thisKeyValueAccessIndexPosition, keyOffset, isPrefixCompressed) =
      EntryWriter.write(
        current = this,
        currentTime = time,
        normaliseToSize = normaliseToSize,
        compressDuplicateValues = valuesConfig.compressDuplicateValues,
        enablePrefixCompression = Transient.enablePrefixCompression(this)
      ).unapply

    override val hasValueEntryBytes: Boolean = previous.exists(_.hasValueEntryBytes) || valueEntryBytes.exists(_.nonEmpty)

    val stats =
      Stats(
        unmergedKeySize = key.size,
        mergedKeySize = mergedKey.size,
        keyOffset = keyOffset,
        indexEntry = indexEntryBytes,
        value = valueEntryBytes,
        isRemoveRange = isRemoveRangeMayBe,
        isRange = isRange,
        isPut = false,
        isPrefixCompressed = isPrefixCompressed,
        sortedIndex = sortedIndexConfig,
        bloomFilter = bloomFilterConfig,
        hashIndex = hashIndexConfig,
        binarySearch = binarySearchIndexConfig,
        values = valuesConfig,
        previousStats = previous.map(_.stats),
        deadline = deadline
      )

    override def updatePrevious(valuesConfig: ValuesBlock.Config,
                                sortedIndexConfig: SortedIndexBlock.Config,
                                binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                                hashIndexConfig: HashIndexBlock.Config,
                                bloomFilterConfig: BloomFilterBlock.Config,
                                previous: Option[Transient]): Transient.Update =
      this.copy(
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig,
        previous = previous
      )

    override def hasTimeLeft(): Boolean =
      deadline.forall(_.hasTimeLeft())
  }

  object Function {
    final val id = 4.toByte
  }

  case class Function(key: Slice[Byte],
                      normaliseToSize: Option[Int],
                      function: Slice[Byte],
                      time: Time,
                      valuesConfig: ValuesBlock.Config,
                      sortedIndexConfig: SortedIndexBlock.Config,
                      binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                      hashIndexConfig: HashIndexBlock.Config,
                      bloomFilterConfig: BloomFilterBlock.Config,
                      previous: Option[Transient]) extends Transient with Transient.Fixed {
    final val id = Function.id
    override val isRemoveRangeMayBe = false
    override val isRange: Boolean = false

    override def mergedKey = key

    override def value: Option[Slice[Byte]] = Some(function)

    override def deadline: Option[Deadline] = None

    val (indexEntryBytes, valueEntryBytes, currentStartValueOffsetPosition, currentEndValueOffsetPosition, thisKeyValueAccessIndexPosition, keyOffset, isPrefixCompressed) =
      EntryWriter.write(
        current = this,
        currentTime = time,
        normaliseToSize = normaliseToSize,
        compressDuplicateValues = valuesConfig.compressDuplicateValues,
        enablePrefixCompression = Transient.enablePrefixCompression(this)
      ).unapply

    override val hasValueEntryBytes: Boolean = previous.exists(_.hasValueEntryBytes) || valueEntryBytes.exists(_.nonEmpty)

    val stats =
      Stats(
        unmergedKeySize = key.size,
        mergedKeySize = mergedKey.size,
        keyOffset = keyOffset,
        indexEntry = indexEntryBytes,
        value = valueEntryBytes,
        isRemoveRange = isRemoveRangeMayBe,
        isRange = isRange,
        isPut = false,
        isPrefixCompressed = isPrefixCompressed,
        sortedIndex = sortedIndexConfig,
        bloomFilter = bloomFilterConfig,
        hashIndex = hashIndexConfig,
        binarySearch = binarySearchIndexConfig,
        values = valuesConfig,
        previousStats = previous.map(_.stats),
        deadline = deadline
      )

    override def updatePrevious(valuesConfig: ValuesBlock.Config,
                                sortedIndexConfig: SortedIndexBlock.Config,
                                binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                                hashIndexConfig: HashIndexBlock.Config,
                                bloomFilterConfig: BloomFilterBlock.Config,
                                previous: Option[Transient]): Transient.Function =
      this.copy(
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig,
        previous = previous
      )

    override def hasTimeLeft(): Boolean =
      deadline.forall(_.hasTimeLeft())
  }

  object PendingApply {
    final val id = 5.toByte
  }

  case class PendingApply(key: Slice[Byte],
                          normaliseToSize: Option[Int],
                          applies: Slice[Value.Apply],
                          valuesConfig: ValuesBlock.Config,
                          sortedIndexConfig: SortedIndexBlock.Config,
                          binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                          hashIndexConfig: HashIndexBlock.Config,
                          bloomFilterConfig: BloomFilterBlock.Config,
                          previous: Option[Transient]) extends Transient with Transient.Fixed {
    final val id = PendingApply.id
    override val isRemoveRangeMayBe = false
    override val isRange: Boolean = false

    override def mergedKey = key

    override val deadline: Option[Deadline] = Segment.getNearestDeadline(None, applies)
    override val value: Option[Slice[Byte]] = Some(ValueSerializer.writeBytes(applies))

    override def time = Time.fromApplies(applies)

    override def updatePrevious(valuesConfig: ValuesBlock.Config,
                                sortedIndexConfig: SortedIndexBlock.Config,
                                binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                                hashIndexConfig: HashIndexBlock.Config,
                                bloomFilterConfig: BloomFilterBlock.Config,
                                previous: Option[Transient]): Transient.PendingApply =
      this.copy(
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig,
        previous = previous
      )

    override def hasTimeLeft(): Boolean =
      true

    val (indexEntryBytes, valueEntryBytes, currentStartValueOffsetPosition, currentEndValueOffsetPosition, thisKeyValueAccessIndexPosition, keyOffset, isPrefixCompressed) =
      EntryWriter.write(
        current = this,
        currentTime = time,
        normaliseToSize = normaliseToSize,
        compressDuplicateValues = valuesConfig.compressDuplicateValues,
        enablePrefixCompression = Transient.enablePrefixCompression(this)
      ).unapply

    override val hasValueEntryBytes: Boolean = previous.exists(_.hasValueEntryBytes) || valueEntryBytes.exists(_.nonEmpty)

    val stats =
      Stats(
        unmergedKeySize = key.size,
        mergedKeySize = mergedKey.size,
        keyOffset = keyOffset,
        indexEntry = indexEntryBytes,
        value = valueEntryBytes,
        isRemoveRange = isRemoveRangeMayBe,
        isRange = isRange,
        isPut = false,
        isPrefixCompressed = isPrefixCompressed,
        sortedIndex = sortedIndexConfig,
        bloomFilter = bloomFilterConfig,
        hashIndex = hashIndexConfig,
        binarySearch = binarySearchIndexConfig,
        values = valuesConfig,
        previousStats = previous.map(_.stats),
        deadline = deadline
      )
  }

  object Range {
    final val id = 6.toByte

    def apply[R <: Value.RangeValue](fromKey: Slice[Byte],
                                     toKey: Slice[Byte],
                                     rangeValue: R,
                                     valuesConfig: ValuesBlock.Config,
                                     sortedIndexConfig: SortedIndexBlock.Config,
                                     binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                                     hashIndexConfig: HashIndexBlock.Config,
                                     bloomFilterConfig: BloomFilterBlock.Config,
                                     previous: Option[Transient])(implicit rangeValueSerializer: RangeValueSerializer[Unit, R]): Range = {

      def valueSerialiser() = {
        val bytesRequired = rangeValueSerializer.bytesRequired((), rangeValue)
        val bytes = if (bytesRequired == 0) None else Some(Slice.create[Byte](bytesRequired))
        bytes.foreach(rangeValueSerializer.write((), rangeValue, _))
        bytes
      }

      val mergedKey = Bytes.compressJoin(fromKey, toKey)
      new Range(
        fromKey = fromKey,
        toKey = toKey,
        mergedKey = mergedKey,
        normaliseToSize = None,
        fromValue = None,
        rangeValue = rangeValue,
        valueSerialiser = valueSerialiser _,
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig,
        previous = previous
      )
    }

    def apply[F <: Value.FromValue, R <: Value.RangeValue](fromKey: Slice[Byte],
                                                           toKey: Slice[Byte],
                                                           fromValue: Option[F],
                                                           rangeValue: R,
                                                           valuesConfig: ValuesBlock.Config,
                                                           sortedIndexConfig: SortedIndexBlock.Config,
                                                           binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                                                           hashIndexConfig: HashIndexBlock.Config,
                                                           bloomFilterConfig: BloomFilterBlock.Config,
                                                           previous: Option[Transient])(implicit rangeValueSerializer: RangeValueSerializer[Option[F], R]): Range = {
      def valueSerialiser() = {
        val bytesRequired = rangeValueSerializer.bytesRequired(fromValue, rangeValue)
        val bytes = if (bytesRequired == 0) None else Some(Slice.create[Byte](bytesRequired))
        bytes.foreach(rangeValueSerializer.write(fromValue, rangeValue, _))
        bytes
      }

      val mergedKey: Slice[Byte] = Bytes.compressJoin(fromKey, toKey)

      new Range(
        fromKey = fromKey,
        toKey = toKey,
        mergedKey = mergedKey,
        normaliseToSize = None,
        fromValue = fromValue,
        rangeValue = rangeValue,
        valueSerialiser = valueSerialiser _,
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig,
        previous = previous
      )
    }
  }

  case class Range(fromKey: Slice[Byte],
                   toKey: Slice[Byte],
                   mergedKey: Slice[Byte],
                   normaliseToSize: Option[Int],
                   fromValue: Option[Value.FromValue],
                   rangeValue: Value.RangeValue,
                   valueSerialiser: () => Option[Slice[Byte]],
                   valuesConfig: ValuesBlock.Config,
                   sortedIndexConfig: SortedIndexBlock.Config,
                   binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                   hashIndexConfig: HashIndexBlock.Config,
                   bloomFilterConfig: BloomFilterBlock.Config,
                   previous: Option[Transient]) extends Transient {
    final val id = Range.id
    override val isRemoveRangeMayBe = rangeValue.hasRemoveMayBe
    override val isRange: Boolean = true
    override val deadline: Option[Deadline] = None

    override def key = fromKey

    override def value = valueSerialiser()

    val (indexEntryBytes, valueEntryBytes, currentStartValueOffsetPosition, currentEndValueOffsetPosition, thisKeyValueAccessIndexPosition, keyOffset, isPrefixCompressed) =
      EntryWriter.write(
        current = this,
        currentTime = Time.empty,
        normaliseToSize = normaliseToSize,
        //It's highly likely that two sequential key-values within the same range have the different value after the range split occurs so this is always set to true.
        compressDuplicateValues = valuesConfig.compressDuplicateRangeValues,
        enablePrefixCompression = Transient.enablePrefixCompression(this)
      ).unapply

    override val hasValueEntryBytes: Boolean = previous.exists(_.hasValueEntryBytes) || valueEntryBytes.exists(_.nonEmpty)

    val stats =
      Stats(
        unmergedKeySize = fromKey.size + toKey.size,
        mergedKeySize = mergedKey.size,
        keyOffset = keyOffset,
        indexEntry = indexEntryBytes,
        value = valueEntryBytes,
        isRemoveRange = isRemoveRangeMayBe,
        isRange = isRange,
        isPut = fromValue.exists(_.isInstanceOf[Value.Put]),
        sortedIndex = sortedIndexConfig,
        isPrefixCompressed = isPrefixCompressed,
        bloomFilter = bloomFilterConfig,
        hashIndex = hashIndexConfig,
        binarySearch = binarySearchIndexConfig,
        values = valuesConfig,
        previousStats = previous.map(_.stats),
        deadline = None
      )

    override def updatePrevious(valuesConfig: ValuesBlock.Config,
                                sortedIndexConfig: SortedIndexBlock.Config,
                                binarySearchIndexConfig: BinarySearchIndexBlock.Config,
                                hashIndexConfig: HashIndexBlock.Config,
                                bloomFilterConfig: BloomFilterBlock.Config,
                                previous: Option[Transient]): Transient.Range =
      this.copy(
        valuesConfig = valuesConfig,
        sortedIndexConfig = sortedIndexConfig,
        binarySearchIndexConfig = binarySearchIndexConfig,
        hashIndexConfig = hashIndexConfig,
        bloomFilterConfig = bloomFilterConfig,
        previous = previous
      )
  }
}

private[core] sealed trait Persistent extends KeyValue.CacheAble with Persistent.Partial {

  def indexOffset: Int
  val nextIndexOffset: Int
  val nextIndexSize: Int
  val sortedIndexAccessPosition: Int

  def valueLength: Int

  def valueOffset: Int

  def toMemory(): Memory

  def isValueCached: Boolean

  def toMemoryResponseOption(): Option[Memory] =
    Some(toMemory())

  /**
   * This function is NOT thread-safe and is mutable. It should always be invoke at the time of creation
   * and before inserting into the Segment's cache.
   */
  def unsliceKeys: Unit
}

private[core] object Persistent {

  /**
   * [[Partial]] key-values types are used in persistent databases
   * for quick search where the entire key-value parse is skipped
   * and only key is read for processing.
   */
  sealed trait Partial {
    def key: Slice[Byte]
    def indexOffset: Int
    def toPersistent: Persistent
  }

  object Partial {
    val noneMaybe: Maybe[Persistent.Partial] =
      null.asInstanceOf[Maybe[Persistent.Partial]]

    trait Fixed extends Persistent.Partial

    trait Range extends Persistent.Partial {
      def fromKey: Slice[Byte]
      def toKey: Slice[Byte]
    }
  }

  sealed trait Fixed extends Persistent with KeyValue.ReadOnly.Fixed with Partial.Fixed

  case class Remove(private var _key: Slice[Byte],
                    deadline: Option[Deadline],
                    private var _time: Time,
                    indexOffset: Int,
                    nextIndexOffset: Int,
                    nextIndexSize: Int,
                    sortedIndexAccessPosition: Int) extends Persistent.Fixed with KeyValue.ReadOnly.Remove {
    override val valueLength: Int = 0
    override val isValueCached: Boolean = true
    override val valueOffset: Int = -1

    def key = _key

    def time = _time

    override def indexEntryDeadline: Option[Deadline] = deadline

    override def unsliceKeys(): Unit = {
      _key = _key.unslice()
      _time = _time.unslice()
    }

    def hasTimeLeft(): Boolean =
      deadline.exists(_.hasTimeLeft())

    def hasTimeLeftAtLeast(minus: FiniteDuration): Boolean =
      deadline.exists(deadline => (deadline - minus).hasTimeLeft())

    override def toMemory(): Memory.Remove =
      Memory.Remove(
        key = key,
        deadline = deadline,
        time = time
      )

    override def copyWithTime(time: Time): ReadOnly.Remove =
      copy(_time = time)

    override def toFromValue(): Value.Remove =
      toRemoveValue()

    override def toRangeValue(): Value.Remove =
      toFromValue()

    override def toRemoveValue(): Value.Remove =
      Value.Remove(deadline, time)

    def toPersistent: Persistent.Remove =
      this
  }

  object Put {
    def apply(key: Slice[Byte],
              deadline: Option[Deadline],
              valuesReader: Option[UnblockedReader[ValuesBlock.Offset, ValuesBlock]],
              time: Time,
              nextIndexOffset: Int,
              nextIndexSize: Int,
              indexOffset: Int,
              valueOffset: Int,
              valueLength: Int,
              sortedIndexAccessPosition: Int) =
      new Put(
        _key = key,
        deadline = deadline,
        valueCache =
          Cache.noIO[ValuesBlock.Offset, Option[Slice[Byte]]](synchronised = true, stored = true, initial = None) {
            (offset, _) =>
              if (offset.size == 0)
                None
              else
                valuesReader match {
                  case Some(valuesReader) =>
                    UnblockedReader.moveTo(offset, valuesReader)
                      .copy()
                      .readFullBlockOrNone()
                      .unslice()

                  case None =>
                    throw IO.throwable("ValuesBlock is undefined.")
                }
          },
        _time = time,
        nextIndexOffset = nextIndexOffset,
        nextIndexSize = nextIndexSize,
        indexOffset = indexOffset,
        valueOffset = valueOffset,
        valueLength = valueLength,
        sortedIndexAccessPosition = sortedIndexAccessPosition
      )
  }

  case class Put(private var _key: Slice[Byte],
                 deadline: Option[Deadline],
                 private val valueCache: CacheNoIO[ValuesBlock.Offset, Option[Slice[Byte]]],
                 private var _time: Time,
                 nextIndexOffset: Int,
                 nextIndexSize: Int,
                 indexOffset: Int,
                 valueOffset: Int,
                 valueLength: Int,
                 sortedIndexAccessPosition: Int) extends Persistent.Fixed with KeyValue.ReadOnly.Put {
    override def unsliceKeys: Unit = {
      _key = _key.unslice()
      _time = _time.unslice()
    }

    override def key: Slice[Byte] =
      _key

    override def time: Time =
      _time

    override def indexEntryDeadline: Option[Deadline] = deadline

    def hasTimeLeft(): Boolean =
      deadline.forall(_.hasTimeLeft())

    def hasTimeLeftAtLeast(minus: FiniteDuration): Boolean =
      deadline.forall(deadline => (deadline - minus).hasTimeLeft())

    override def getOrFetchValue: Option[Slice[Byte]] =
      valueCache.value(ValuesBlock.Offset(valueOffset, valueLength))

    override def isValueCached: Boolean =
      valueCache.isCached

    override def toFromValue(): Value.Put =
      Value.Put(
        value = getOrFetchValue,
        deadline = deadline,
        time = time
      )

    override def toRangeValue(): Value.RangeValue =
      throw IO.throwable("Put cannot be converted to RangeValue")

    override def toMemory(): Memory.Put =
      Memory.Put(
        key = key,
        value = getOrFetchValue,
        deadline = deadline,
        time = time
      )

    override def copyWithDeadlineAndTime(deadline: Option[Deadline],
                                         time: Time): Put =
      copy(deadline = deadline, _time = time)

    override def copyWithTime(time: Time): Put =
      copy(_time = time)

    def toPersistent: Persistent.Put =
      this
  }

  object Update {
    def apply(key: Slice[Byte],
              deadline: Option[Deadline],
              valuesReader: Option[UnblockedReader[ValuesBlock.Offset, ValuesBlock]],
              time: Time,
              nextIndexOffset: Int,
              nextIndexSize: Int,
              indexOffset: Int,
              valueOffset: Int,
              valueLength: Int,
              sortedIndexAccessPosition: Int) =
      new Update(
        _key = key,
        deadline = deadline,
        valueCache =
          Cache.noIO[ValuesBlock.Offset, Option[Slice[Byte]]](synchronised = true, stored = true, initial = None) {
            (offset, _) =>
              if (offset.size == 0)
                None
              else
                valuesReader match {
                  case Some(valuesReader) =>
                    UnblockedReader.moveTo(offset, valuesReader)
                      .copy()
                      .readFullBlockOrNone()
                      .unslice()

                  case None =>
                    throw IO.throwable("ValuesBlock is undefined.")
                }
          },
        _time = time,
        nextIndexOffset = nextIndexOffset,
        nextIndexSize = nextIndexSize,
        indexOffset = indexOffset,
        valueOffset = valueOffset,
        valueLength = valueLength,
        sortedIndexAccessPosition = sortedIndexAccessPosition
      )
  }

  case class Update(private var _key: Slice[Byte],
                    deadline: Option[Deadline],
                    private val valueCache: CacheNoIO[ValuesBlock.Offset, Option[Slice[Byte]]],
                    private var _time: Time,
                    nextIndexOffset: Int,
                    nextIndexSize: Int,
                    indexOffset: Int,
                    valueOffset: Int,
                    valueLength: Int,
                    sortedIndexAccessPosition: Int) extends Persistent.Fixed with KeyValue.ReadOnly.Update {
    override def unsliceKeys: Unit = {
      _key = _key.unslice()
      _time = _time.unslice()
    }

    override def key: Slice[Byte] =
      _key

    override def time: Time =
      _time

    override def indexEntryDeadline: Option[Deadline] = deadline

    def hasTimeLeft(): Boolean =
      deadline.forall(_.hasTimeLeft())

    def hasTimeLeftAtLeast(minus: FiniteDuration): Boolean =
      deadline.forall(deadline => (deadline - minus).hasTimeLeft())

    override def isValueCached: Boolean =
      valueCache.isCached

    def getOrFetchValue: Option[Slice[Byte]] =
      valueCache.value(ValuesBlock.Offset(valueOffset, valueLength))

    override def toFromValue(): Value.Update =
      Value.Update(
        value = getOrFetchValue,
        deadline = deadline,
        time = time
      )

    override def toRangeValue(): Value.Update =
      toFromValue()

    override def toMemory(): Memory.Update =
      Memory.Update(
        key = key,
        value = getOrFetchValue,
        deadline = deadline,
        time = time
      )

    override def copyWithDeadlineAndTime(deadline: Option[Deadline],
                                         time: Time): Update =
      copy(deadline = deadline, _time = time)

    override def copyWithDeadline(deadline: Option[Deadline]): Update =
      copy(deadline = deadline)

    override def copyWithTime(time: Time): Update =
      copy(_time = time)

    override def toPut(): Persistent.Put =
      Persistent.Put(
        _key = key,
        deadline = deadline,
        valueCache = valueCache,
        _time = time,
        nextIndexOffset = nextIndexOffset,
        nextIndexSize = nextIndexSize,
        indexOffset = indexOffset,
        valueOffset = valueOffset,
        valueLength = valueLength,
        sortedIndexAccessPosition = sortedIndexAccessPosition
      )

    override def toPut(deadline: Option[Deadline]): Persistent.Put =
      Persistent.Put(
        _key = key,
        deadline = deadline,
        valueCache = valueCache,
        _time = time,
        nextIndexOffset = nextIndexOffset,
        nextIndexSize = nextIndexSize,
        indexOffset = indexOffset,
        valueOffset = valueOffset,
        valueLength = valueLength,
        sortedIndexAccessPosition = sortedIndexAccessPosition
      )

    def toPersistent: Persistent.Update =
      this
  }

  object Function {
    def apply(key: Slice[Byte],
              valuesReader: Option[UnblockedReader[ValuesBlock.Offset, ValuesBlock]],
              time: Time,
              nextIndexOffset: Int,
              nextIndexSize: Int,
              indexOffset: Int,
              valueOffset: Int,
              valueLength: Int,
              sortedIndexAccessPosition: Int) =
      new Function(
        _key = key,
        valueCache =
          Cache.noIO[ValuesBlock.Offset, Slice[Byte]](synchronised = true, stored = true, initial = None) {
            (offset, _) =>
              valuesReader match {
                case Some(valuesReader) =>
                  UnblockedReader.moveTo(offset, valuesReader)
                    .copy()
                    .readFullBlock()
                    .unslice()

                case None =>
                  throw IO.throwable("ValuesBlock is undefined.")
              }
          },
        _time = time,
        nextIndexOffset = nextIndexOffset,
        nextIndexSize = nextIndexSize,
        indexOffset = indexOffset,
        valueOffset = valueOffset,
        valueLength = valueLength,
        sortedIndexAccessPosition = sortedIndexAccessPosition
      )
  }

  case class Function(private var _key: Slice[Byte],
                      private val valueCache: CacheNoIO[ValuesBlock.Offset, Slice[Byte]],
                      private var _time: Time,
                      nextIndexOffset: Int,
                      nextIndexSize: Int,
                      indexOffset: Int,
                      valueOffset: Int,
                      valueLength: Int,
                      sortedIndexAccessPosition: Int) extends Persistent.Fixed with KeyValue.ReadOnly.Function {
    override def unsliceKeys: Unit = {
      _key = _key.unslice()
      _time = _time.unslice()
    }

    override def key: Slice[Byte] =
      _key

    override def time: Time =
      _time

    override def indexEntryDeadline: Option[Deadline] = None

    override def isValueCached: Boolean =
      valueCache.isCached

    def getOrFetchFunction: Slice[Byte] =
      valueCache.value(ValuesBlock.Offset(valueOffset, valueLength))

    override def toFromValue(): Value.Function =
      Value.Function(
        function = getOrFetchFunction,
        time = time
      )

    override def toRangeValue(): Value.Function =
      toFromValue()

    override def toMemory(): Memory.Function =
      Memory.Function(
        key = key,
        function = getOrFetchFunction,
        time = time
      )

    override def copyWithTime(time: Time): Function =
      copy(_time = time)

    def toPersistent: Persistent.Function =
      this
  }

  object PendingApply {
    def apply(key: Slice[Byte],
              time: Time,
              deadline: Option[Deadline],
              valuesReader: Option[UnblockedReader[ValuesBlock.Offset, ValuesBlock]],
              nextIndexOffset: Int,
              nextIndexSize: Int,
              indexOffset: Int,
              valueOffset: Int,
              valueLength: Int,
              sortedIndexAccessPosition: Int) =
      new PendingApply(
        _key = key,
        _time = time,
        deadline = deadline,
        valueCache =
          Cache.noIO[ValuesBlock.Offset, Slice[Value.Apply]](synchronised = true, stored = true, initial = None) {
            (offset, _) =>
              valuesReader match {
                case Some(valuesReader) =>
                  val bytes =
                    UnblockedReader.moveTo(offset, valuesReader)
                      .copy()
                      .readFullBlock()

                  ValueSerializer
                    .read[Slice[Value.Apply]](bytes)
                    .map(_.unslice)

                case None =>
                  throw IO.throwable("ValuesBlock is undefined.")
              }
          },
        nextIndexOffset = nextIndexOffset,
        nextIndexSize = nextIndexSize,
        indexOffset = indexOffset,
        valueOffset = valueOffset,
        valueLength = valueLength,
        sortedIndexAccessPosition = sortedIndexAccessPosition
      )
  }

  case class PendingApply(private var _key: Slice[Byte],
                          private var _time: Time,
                          deadline: Option[Deadline],
                          valueCache: CacheNoIO[ValuesBlock.Offset, Slice[Value.Apply]],
                          nextIndexOffset: Int,
                          nextIndexSize: Int,
                          indexOffset: Int,
                          valueOffset: Int,
                          valueLength: Int,
                          sortedIndexAccessPosition: Int) extends Persistent.Fixed with KeyValue.ReadOnly.PendingApply {
    override def unsliceKeys: Unit = {
      _key = _key.unslice()
      _time = _time.unslice()
    }

    override def key: Slice[Byte] =
      _key

    override def time: Time =
      _time

    override def indexEntryDeadline: Option[Deadline] = deadline

    override def isValueCached: Boolean =
      valueCache.isCached

    override def getOrFetchApplies: Slice[Value.Apply] =
      valueCache.value(ValuesBlock.Offset(valueOffset, valueLength))

    override def toFromValue(): Value.PendingApply = {
      val applies = valueCache.value(ValuesBlock.Offset(valueOffset, valueLength))
      Value.PendingApply(applies)
    }

    override def toRangeValue(): Value.PendingApply =
      toFromValue()

    override def toMemory(): Memory.PendingApply =
      Memory.PendingApply(
        key = key,
        applies = getOrFetchApplies
      )

    def toPersistent: Persistent.PendingApply =
      this
  }

  object Range {
    def apply(key: Slice[Byte],
              valuesReader: Option[UnblockedReader[ValuesBlock.Offset, ValuesBlock]],
              nextIndexOffset: Int,
              nextIndexSize: Int,
              indexOffset: Int,
              valueOffset: Int,
              valueLength: Int,
              sortedIndexAccessPosition: Int): Range = {
      val (fromKey, toKey) = Bytes.decompressJoin(key)
      Range.parsedKey(
        fromKey = fromKey,
        toKey = toKey,
        valuesReader = valuesReader,
        nextIndexOffset = nextIndexOffset,
        nextIndexSize = nextIndexSize,
        indexOffset = indexOffset,
        valueOffset = valueOffset,
        valueLength = valueLength,
        sortedIndexAccessPosition = sortedIndexAccessPosition
      )
    }

    def parsedKey(fromKey: Slice[Byte],
                  toKey: Slice[Byte],
                  valuesReader: Option[UnblockedReader[ValuesBlock.Offset, ValuesBlock]],
                  nextIndexOffset: Int,
                  nextIndexSize: Int,
                  indexOffset: Int,
                  valueOffset: Int,
                  valueLength: Int,
                  sortedIndexAccessPosition: Int): Persistent.Range =
      Range(
        _fromKey = fromKey,
        _toKey = toKey,
        valueCache =
          Cache.noIO[ValuesBlock.Offset, (Option[Value.FromValue], Value.RangeValue)](synchronised = true, stored = true, initial = None) {
            (offset, _) =>
              valuesReader match {
                case Some(valuesReader) =>
                  val bytes =
                    UnblockedReader.moveTo(offset, valuesReader)
                      .copy()
                      .readFullBlock()

                  val (from, range) =
                    RangeValueSerializer.read(bytes)

                  (from.map(_.unslice), range.unslice)

                case None =>
                  throw IO.throwable("ValuesBlock is undefined.")
              }
          },
        nextIndexOffset = nextIndexOffset,
        nextIndexSize = nextIndexSize,
        indexOffset = indexOffset,
        valueOffset = valueOffset,
        valueLength = valueLength,
        sortedIndexAccessPosition = sortedIndexAccessPosition
      )
  }

  case class Range private(private var _fromKey: Slice[Byte],
                           private var _toKey: Slice[Byte],
                           valueCache: CacheNoIO[ValuesBlock.Offset, (Option[Value.FromValue], Value.RangeValue)],
                           nextIndexOffset: Int,
                           nextIndexSize: Int,
                           indexOffset: Int,
                           valueOffset: Int,
                           valueLength: Int,
                           sortedIndexAccessPosition: Int) extends Persistent with KeyValue.ReadOnly.Range with Partial.Range {

    def fromKey = _fromKey

    def toKey = _toKey

    override def indexEntryDeadline: Option[Deadline] = None

    override def unsliceKeys: Unit = {
      this._fromKey = _fromKey.unslice()
      this._toKey = _toKey.unslice()
    }

    override def key: Slice[Byte] =
      _fromKey

    def fetchRangeValueUnsafe: Value.RangeValue =
      fetchFromAndRangeValueUnsafe._2

    def fetchFromValueUnsafe: Option[Value.FromValue] =
      fetchFromAndRangeValueUnsafe._1

    def fetchFromAndRangeValueUnsafe: (Option[Value.FromValue], Value.RangeValue) =
      valueCache.value(ValuesBlock.Offset(valueOffset, valueLength))

    override def toMemory(): Memory.Range = {
      val (fromValue, rangeValue) = fetchFromAndRangeValueUnsafe
      Memory.Range(
        fromKey = fromKey,
        toKey = toKey,
        fromValue = fromValue,
        rangeValue = rangeValue
      )
    }

    override def isValueCached: Boolean =
      valueCache.isCached

    def toPersistent: Persistent.Range =
      this
  }
}
