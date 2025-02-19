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

package swaydb.core.segment.format.a.entry.reader.base

import swaydb.core.data.Persistent
import swaydb.core.segment.format.a.block.ValuesBlock
import swaydb.core.segment.format.a.block.reader.UnblockedReader
import swaydb.core.segment.format.a.entry.id.BaseEntryIdFormatA
import swaydb.core.segment.format.a.entry.reader.EntryReader
import swaydb.data.slice.ReaderBase
import swaydb.data.slice.{ReaderBase, Slice}

private[core] object BaseEntryReader1 extends BaseEntryReader {

  def read[T](baseId: Int,
              keyValueId: Int,
              sortedIndexAccessPosition: Int,
              keyOption: Option[Slice[Byte]],
              indexReader: ReaderBase,
              valuesReader: Option[UnblockedReader[ValuesBlock.Offset, ValuesBlock]],
              indexOffset: Int,
              nextIndexOffset: Int,
              nextIndexSize: Int,
              previous: Option[Persistent],
              reader: EntryReader[T]): T =
  //GENERATED CONDITIONS
    if (baseId == 172)
      reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
    else if (baseId < 172)
      if (baseId == 86)
        reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
      else if (baseId < 86)
        if (baseId == 43)
          reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
        else if (baseId < 43)
          if (baseId == 21)
            reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
          else if (baseId < 21)
            if (baseId == 10)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 10)
              if (baseId == 5)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 5)
                if (baseId == 2)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 2)
                  if (baseId == 0)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 1)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 2)
                  if (baseId == 3)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 4)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.NoValue.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 5)
                if (baseId == 8)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 8)
                  if (baseId == 6)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 7)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 8)
                  if (baseId == 9)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 10)
              if (baseId == 16)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 16)
                if (baseId == 13)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetUncompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 13)
                  if (baseId == 11)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 12)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 13)
                  if (baseId == 14)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.NoValue.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 15)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 16)
                if (baseId == 19)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 19)
                  if (baseId == 17)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 18)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 19)
                  if (baseId == 20)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else if (baseId > 21)
            if (baseId == 32)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 32)
              if (baseId == 27)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 27)
                if (baseId == 24)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.NoValue.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 24)
                  if (baseId == 22)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 23)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 24)
                  if (baseId == 25)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 26)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 27)
                if (baseId == 30)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 30)
                  if (baseId == 28)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 29)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthUncompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 30)
                  if (baseId == 31)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 32)
              if (baseId == 38)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 38)
                if (baseId == 35)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 35)
                  if (baseId == 33)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 34)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 35)
                  if (baseId == 36)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 37)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 38)
                if (baseId == 41)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 41)
                  if (baseId == 39)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 40)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 41)
                  if (baseId == 42)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else
            throw swaydb.Exception.InvalidKeyValueId(baseId)
        else if (baseId > 43)
          if (baseId == 65)
            reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
          else if (baseId < 65)
            if (baseId == 54)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 54)
              if (baseId == 49)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 49)
                if (baseId == 46)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 46)
                  if (baseId == 44)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 45)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 46)
                  if (baseId == 47)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 48)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 49)
                if (baseId == 52)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 52)
                  if (baseId == 50)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.NoValue.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 51)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 52)
                  if (baseId == 53)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 54)
              if (baseId == 60)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 60)
                if (baseId == 57)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 57)
                  if (baseId == 55)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 56)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 57)
                  if (baseId == 58)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 59)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 60)
                if (baseId == 63)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 63)
                  if (baseId == 61)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 62)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 63)
                  if (baseId == 64)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else if (baseId > 65)
            if (baseId == 76)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 76)
              if (baseId == 71)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 71)
                if (baseId == 68)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 68)
                  if (baseId == 66)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 67)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 68)
                  if (baseId == 69)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 70)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 71)
                if (baseId == 74)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 74)
                  if (baseId == 72)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 73)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 74)
                  if (baseId == 75)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 76)
              if (baseId == 81)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 81)
                if (baseId == 79)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 79)
                  if (baseId == 77)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 78)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 79)
                  if (baseId == 80)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 81)
                if (baseId == 84)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 84)
                  if (baseId == 82)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 83)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 84)
                  if (baseId == 85)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else
            throw swaydb.Exception.InvalidKeyValueId(baseId)
        else
          throw swaydb.Exception.InvalidKeyValueId(baseId)
      else if (baseId > 86)
        if (baseId == 129)
          reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
        else if (baseId < 129)
          if (baseId == 108)
            reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
          else if (baseId < 108)
            if (baseId == 97)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 97)
              if (baseId == 92)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetUncompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 92)
                if (baseId == 89)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 89)
                  if (baseId == 87)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 88)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 89)
                  if (baseId == 90)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 91)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetUncompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 92)
                if (baseId == 95)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetUncompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 95)
                  if (baseId == 93)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetUncompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 94)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetUncompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 95)
                  if (baseId == 96)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.NoValue.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 97)
              if (baseId == 103)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 103)
                if (baseId == 100)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 100)
                  if (baseId == 98)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 99)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 100)
                  if (baseId == 101)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 102)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 103)
                if (baseId == 106)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 106)
                  if (baseId == 104)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 105)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 106)
                  if (baseId == 107)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else if (baseId > 108)
            if (baseId == 119)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 119)
              if (baseId == 114)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 114)
                if (baseId == 111)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 111)
                  if (baseId == 109)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 110)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 111)
                  if (baseId == 112)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 113)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 114)
                if (baseId == 117)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 117)
                  if (baseId == 115)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 116)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 117)
                  if (baseId == 118)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 119)
              if (baseId == 124)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 124)
                if (baseId == 122)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 122)
                  if (baseId == 120)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 121)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 122)
                  if (baseId == 123)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 124)
                if (baseId == 127)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 127)
                  if (baseId == 125)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 126)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 127)
                  if (baseId == 128)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else
            throw swaydb.Exception.InvalidKeyValueId(baseId)
        else if (baseId > 129)
          if (baseId == 151)
            reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
          else if (baseId < 151)
            if (baseId == 140)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 140)
              if (baseId == 135)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 135)
                if (baseId == 132)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 132)
                  if (baseId == 130)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 131)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 132)
                  if (baseId == 133)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 134)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 135)
                if (baseId == 138)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 138)
                  if (baseId == 136)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 137)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 138)
                  if (baseId == 139)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 140)
              if (baseId == 146)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 146)
                if (baseId == 143)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 143)
                  if (baseId == 141)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 142)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.NoValue.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 143)
                  if (baseId == 144)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 145)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 146)
                if (baseId == 149)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 149)
                  if (baseId == 147)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 148)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 149)
                  if (baseId == 150)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else if (baseId > 151)
            if (baseId == 162)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 162)
              if (baseId == 157)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 157)
                if (baseId == 154)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 154)
                  if (baseId == 152)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 153)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 154)
                  if (baseId == 155)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 156)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 157)
                if (baseId == 160)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 160)
                  if (baseId == 158)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 159)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 160)
                  if (baseId == 161)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 162)
              if (baseId == 167)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthUncompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 167)
                if (baseId == 165)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthThreeCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 165)
                  if (baseId == 163)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthOneCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 164)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthTwoCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 165)
                  if (baseId == 166)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthFullyCompressed.DeadlineUncompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 167)
                if (baseId == 170)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 170)
                  if (baseId == 168)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 169)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 170)
                  if (baseId == 171)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else
            throw swaydb.Exception.InvalidKeyValueId(baseId)
        else
          throw swaydb.Exception.InvalidKeyValueId(baseId)
      else
        throw swaydb.Exception.InvalidKeyValueId(baseId)
    else if (baseId > 172)
      if (baseId == 259)
        reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
      else if (baseId < 259)
        if (baseId == 216)
          reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetUncompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
        else if (baseId < 216)
          if (baseId == 194)
            reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
          else if (baseId < 194)
            if (baseId == 183)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 183)
              if (baseId == 178)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 178)
                if (baseId == 175)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 175)
                  if (baseId == 173)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 174)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 175)
                  if (baseId == 176)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 177)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 178)
                if (baseId == 181)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 181)
                  if (baseId == 179)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 180)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 181)
                  if (baseId == 182)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 183)
              if (baseId == 189)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 189)
                if (baseId == 186)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 186)
                  if (baseId == 184)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 185)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 186)
                  if (baseId == 187)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 188)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 189)
                if (baseId == 192)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 192)
                  if (baseId == 190)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 191)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 192)
                  if (baseId == 193)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else if (baseId > 194)
            if (baseId == 205)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 205)
              if (baseId == 200)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 200)
                if (baseId == 197)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 197)
                  if (baseId == 195)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 196)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 197)
                  if (baseId == 198)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 199)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 200)
                if (baseId == 203)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 203)
                  if (baseId == 201)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 202)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 203)
                  if (baseId == 204)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 205)
              if (baseId == 211)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 211)
                if (baseId == 208)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 208)
                  if (baseId == 206)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 207)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 208)
                  if (baseId == 209)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 210)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 211)
                if (baseId == 214)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 214)
                  if (baseId == 212)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 213)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 214)
                  if (baseId == 215)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else
            throw swaydb.Exception.InvalidKeyValueId(baseId)
        else if (baseId > 216)
          if (baseId == 238)
            reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
          else if (baseId < 238)
            if (baseId == 227)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 227)
              if (baseId == 222)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 222)
                if (baseId == 219)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetUncompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 219)
                  if (baseId == 217)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetUncompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 218)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueUncompressed.ValueOffsetUncompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 219)
                  if (baseId == 220)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 221)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 222)
                if (baseId == 225)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 225)
                  if (baseId == 223)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 224)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 225)
                  if (baseId == 226)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 227)
              if (baseId == 233)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 233)
                if (baseId == 230)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 230)
                  if (baseId == 228)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 229)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 230)
                  if (baseId == 231)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 232)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 233)
                if (baseId == 236)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 236)
                  if (baseId == 234)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 235)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 236)
                  if (baseId == 237)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else if (baseId > 238)
            if (baseId == 249)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 249)
              if (baseId == 244)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 244)
                if (baseId == 241)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 241)
                  if (baseId == 239)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.NoTime.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 240)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 241)
                  if (baseId == 242)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 243)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 244)
                if (baseId == 247)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 247)
                  if (baseId == 245)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 246)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 247)
                  if (baseId == 248)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 249)
              if (baseId == 254)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 254)
                if (baseId == 252)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 252)
                  if (baseId == 250)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 251)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetThreeCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 252)
                  if (baseId == 253)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 254)
                if (baseId == 257)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 257)
                  if (baseId == 255)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueUncompressed.ValueOffsetUncompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 256)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 257)
                  if (baseId == 258)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else
            throw swaydb.Exception.InvalidKeyValueId(baseId)
        else
          throw swaydb.Exception.InvalidKeyValueId(baseId)
      else if (baseId > 259)
        if (baseId == 302)
          reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineThreeCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
        else if (baseId < 302)
          if (baseId == 281)
            reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineSixCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
          else if (baseId < 281)
            if (baseId == 270)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 270)
              if (baseId == 265)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 265)
                if (baseId == 262)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 262)
                  if (baseId == 260)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 261)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 262)
                  if (baseId == 263)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 264)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 265)
                if (baseId == 268)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 268)
                  if (baseId == 266)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 267)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetThreeCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 268)
                  if (baseId == 269)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 270)
              if (baseId == 276)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineOneCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 276)
                if (baseId == 273)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthTwoCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 273)
                  if (baseId == 271)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetFullyCompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 272)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthOneCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 273)
                  if (baseId == 274)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthThreeCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 275)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimeUncompressed.ValueFullyCompressed.ValueOffsetUncompressed.ValueLengthFullyCompressed.NoDeadline, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 276)
                if (baseId == 279)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineFourCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 279)
                  if (baseId == 277)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineTwoCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 278)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineThreeCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 279)
                  if (baseId == 280)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineFiveCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else if (baseId > 281)
            if (baseId == 292)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineOneCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 292)
              if (baseId == 287)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineFourCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 287)
                if (baseId == 284)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineOneCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 284)
                  if (baseId == 282)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineSevenCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 283)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthOneCompressed.DeadlineFullyCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 284)
                  if (baseId == 285)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineTwoCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 286)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineThreeCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 287)
                if (baseId == 290)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineSevenCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 290)
                  if (baseId == 288)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineFiveCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 289)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineSixCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 290)
                  if (baseId == 291)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthTwoCompressed.DeadlineFullyCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 292)
              if (baseId == 297)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineSixCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 297)
                if (baseId == 295)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineFourCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 295)
                  if (baseId == 293)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineTwoCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 294)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineThreeCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 295)
                  if (baseId == 296)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineFiveCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 297)
                if (baseId == 300)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineOneCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 300)
                  if (baseId == 298)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineSevenCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 299)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthThreeCompressed.DeadlineFullyCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 300)
                  if (baseId == 301)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineTwoCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else
            throw swaydb.Exception.InvalidKeyValueId(baseId)
        else if (baseId > 302)
          if (baseId == 324)
            reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineOneCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
          else if (baseId < 324)
            if (baseId == 313)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineSixCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 313)
              if (baseId == 308)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineOneCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 308)
                if (baseId == 305)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineSixCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 305)
                  if (baseId == 303)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineFourCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 304)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineFiveCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 305)
                  if (baseId == 306)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineSevenCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 307)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthFullyCompressed.DeadlineFullyCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 308)
                if (baseId == 311)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineFourCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 311)
                  if (baseId == 309)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineTwoCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 310)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineThreeCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 311)
                  if (baseId == 312)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineFiveCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 313)
              if (baseId == 319)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineFourCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 319)
                if (baseId == 316)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineOneCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 316)
                  if (baseId == 314)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineSevenCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 315)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetOneCompressed.ValueLengthUncompressed.DeadlineFullyCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 316)
                  if (baseId == 317)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineTwoCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 318)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineThreeCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 319)
                if (baseId == 322)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineSevenCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 322)
                  if (baseId == 320)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineFiveCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 321)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineSixCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 322)
                  if (baseId == 323)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthOneCompressed.DeadlineFullyCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else if (baseId > 324)
            if (baseId == 335)
              reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineFourCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
            else if (baseId < 335)
              if (baseId == 330)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineSevenCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 330)
                if (baseId == 327)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineFourCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 327)
                  if (baseId == 325)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineTwoCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 326)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineThreeCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 327)
                  if (baseId == 328)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineFiveCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 329)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineSixCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 330)
                if (baseId == 333)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineTwoCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 333)
                  if (baseId == 331)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthTwoCompressed.DeadlineFullyCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 332)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineOneCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 333)
                  if (baseId == 334)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineThreeCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else if (baseId > 335)
              if (baseId == 340)
                reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.DeadlineOneCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
              else if (baseId < 340)
                if (baseId == 338)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineSevenCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 338)
                  if (baseId == 336)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineFiveCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 337)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineSixCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 338)
                  if (baseId == 339)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthThreeCompressed.DeadlineFullyCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else if (baseId > 340)
                if (baseId == 343)
                  reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.DeadlineFourCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                else if (baseId < 343)
                  if (baseId == 341)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.DeadlineTwoCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else if (baseId == 342)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.DeadlineThreeCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else if (baseId > 343)
                  if (baseId == 344)
                    reader(BaseEntryIdFormatA.FormatA1.KeyStart.TimePartiallyCompressed.ValueUncompressed.ValueOffsetTwoCompressed.ValueLengthFullyCompressed.DeadlineFiveCompressed, keyValueId, sortedIndexAccessPosition, keyOption, indexReader, valuesReader, indexOffset, nextIndexOffset, nextIndexSize, previous)
                  else
                    throw swaydb.Exception.InvalidKeyValueId(baseId)
                else
                  throw swaydb.Exception.InvalidKeyValueId(baseId)
              else
                throw swaydb.Exception.InvalidKeyValueId(baseId)
            else
              throw swaydb.Exception.InvalidKeyValueId(baseId)
          else
            throw swaydb.Exception.InvalidKeyValueId(baseId)
        else
          throw swaydb.Exception.InvalidKeyValueId(baseId)
      else
        throw swaydb.Exception.InvalidKeyValueId(baseId)
    else
      throw swaydb.Exception.InvalidKeyValueId(baseId)

  val minID = 0
  val maxID = 344
}