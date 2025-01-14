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

package swaydb.core.segment.format.a.entry.reader

import swaydb.IO
import swaydb.core.data.Persistent
import swaydb.core.segment.format.a.entry.id.BaseEntryId
import swaydb.core.util.Bytes
import swaydb.core.util.Times._
import swaydb.data.slice.ReaderBase
import swaydb.data.util.ByteSizeOf

import scala.annotation.implicitNotFound
import scala.concurrent.duration

@implicitNotFound("Type class implementation not found for DeadlineReader of type ${T}")
sealed trait DeadlineReader[-T] {
  def isPrefixCompressed: Boolean

  def read(indexReader: ReaderBase,
           previous: Option[Persistent]): Option[duration.Deadline]
}

object DeadlineReader {
  implicit object NoDeadlineReader extends DeadlineReader[BaseEntryId.Deadline.NoDeadline] {
    override def isPrefixCompressed: Boolean = false

    override def read(indexReader: ReaderBase,
                      previous: Option[Persistent]): Option[duration.Deadline] =
      None
  }

  implicit object DeadlineFullyCompressedReader extends DeadlineReader[BaseEntryId.Deadline.FullyCompressed] {
    override def isPrefixCompressed: Boolean = true

    override def read(indexReader: ReaderBase,
                      previous: Option[Persistent]): Option[duration.Deadline] =
      previous match {
        case Some(previous) =>
          previous match {
            case previous: Persistent =>
              previous.indexEntryDeadline match {
                case some @ Some(_) =>
                  some
                case None =>
                  throw EntryReaderFailure.NoPreviousDeadline
              }

            case _ =>
              throw IO.throwable("Expected Persistent. Received Partial")
          }
        case None =>
          throw EntryReaderFailure.NoPreviousKeyValue
      }
  }

  private def decompressDeadline(indexReader: ReaderBase,
                                 commonBytes: Int,
                                 previous: Option[Persistent]): Option[duration.Deadline] =
    previous match {
      case Some(previous: Persistent) =>
        previous.indexEntryDeadline match {
          case Some(previousDeadline) =>
            Bytes
              .decompress(
                previous = previousDeadline.toBytes,
                next = indexReader.read(ByteSizeOf.long - commonBytes),
                commonBytes = commonBytes
              )
              .readLong()
              .toDeadlineOption

          case None =>
            throw EntryReaderFailure.NoPreviousDeadline
        }

      case None =>
        throw EntryReaderFailure.NoPreviousKeyValue
    }

  implicit object DeadlineOneCompressedReader extends DeadlineReader[BaseEntryId.Deadline.OneCompressed] {
    override def isPrefixCompressed: Boolean = true

    override def read(indexReader: ReaderBase,
                      previous: Option[Persistent]): Option[duration.Deadline] =
      decompressDeadline(indexReader = indexReader, commonBytes = 1, previous = previous)
  }

  implicit object DeadlineTwoCompressedReader extends DeadlineReader[BaseEntryId.Deadline.TwoCompressed] {
    override def isPrefixCompressed: Boolean = true

    override def read(indexReader: ReaderBase,
                      previous: Option[Persistent]): Option[duration.Deadline] =
      decompressDeadline(indexReader = indexReader, commonBytes = 2, previous = previous)
  }

  implicit object DeadlineThreeCompressedReader extends DeadlineReader[BaseEntryId.Deadline.ThreeCompressed] {
    override def isPrefixCompressed: Boolean = true

    override def read(indexReader: ReaderBase,
                      previous: Option[Persistent]): Option[duration.Deadline] =
      decompressDeadline(indexReader = indexReader, commonBytes = 3, previous = previous)
  }

  implicit object DeadlineFourCompressedReader extends DeadlineReader[BaseEntryId.Deadline.FourCompressed] {
    override def isPrefixCompressed: Boolean = true

    override def read(indexReader: ReaderBase,
                      previous: Option[Persistent]): Option[duration.Deadline] =
      decompressDeadline(indexReader = indexReader, commonBytes = 4, previous = previous)
  }

  implicit object DeadlineFiveCompressedReader extends DeadlineReader[BaseEntryId.Deadline.FiveCompressed] {
    override def isPrefixCompressed: Boolean = true

    override def read(indexReader: ReaderBase,
                      previous: Option[Persistent]): Option[duration.Deadline] =
      decompressDeadline(indexReader = indexReader, commonBytes = 5, previous = previous)
  }

  implicit object DeadlineSixCompressedReader extends DeadlineReader[BaseEntryId.Deadline.SixCompressed] {
    override def isPrefixCompressed: Boolean = true

    override def read(indexReader: ReaderBase,
                      previous: Option[Persistent]): Option[duration.Deadline] =
      decompressDeadline(indexReader = indexReader, commonBytes = 6, previous = previous)
  }

  implicit object DeadlineSevenCompressedReader extends DeadlineReader[BaseEntryId.Deadline.SevenCompressed] {
    override def isPrefixCompressed: Boolean = true

    override def read(indexReader: ReaderBase,
                      previous: Option[Persistent]): Option[duration.Deadline] =
      decompressDeadline(indexReader = indexReader, commonBytes = 7, previous = previous)
  }

  implicit object DeadlineUncompressedReader extends DeadlineReader[BaseEntryId.Deadline.Uncompressed] {
    override def isPrefixCompressed: Boolean = false

    override def read(indexReader: ReaderBase,
                      previous: Option[Persistent]): Option[duration.Deadline] =
      indexReader.readUnsignedLong().toDeadlineOption
  }
}
