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

import swaydb.core.data.Persistent
import swaydb.core.segment.format.a.entry.id.BaseEntryId
import swaydb.data.slice.ReaderBase

import scala.annotation.implicitNotFound

@implicitNotFound("Type class implementation not found for ValueReader of type ${T}")
sealed trait ValueReader[-T] {

  def isPrefixCompressed: Boolean

  def read[V](indexReader: ReaderBase,
              previous: Option[Persistent])(implicit valueOffsetReader: ValueOffsetReader[V],
                                            valueLengthReader: ValueLengthReader[V]): Option[(Int, Int)]
}

object ValueReader {
  implicit object NoValueReader extends ValueReader[BaseEntryId.Value.NoValue] {
    override def isPrefixCompressed: Boolean = false

    override def read[V](indexReader: ReaderBase,
                         previous: Option[Persistent])(implicit valueOffsetReader: ValueOffsetReader[V],
                                                       valueLengthReader: ValueLengthReader[V]): Option[(Int, Int)] =
      None
  }

  implicit object ValueUncompressedReader extends ValueReader[BaseEntryId.Value.Uncompressed] {
    override def isPrefixCompressed: Boolean = false
    override def read[V](indexReader: ReaderBase,
                         previous: Option[Persistent])(implicit valueOffsetReader: ValueOffsetReader[V],
                                                       valueLengthReader: ValueLengthReader[V]): Option[(Int, Int)] = {
      val valueOffset = valueOffsetReader.read(indexReader, previous)
      val valueLength = valueLengthReader.read(indexReader, previous)
      Some((valueOffset, valueLength))
    }
  }

  implicit object ValueFullyCompressedReader extends ValueReader[BaseEntryId.Value.FullyCompressed] {
    //prefixCompression does not apply on the value itself since it can still hold reference to offset and length.
    //A value is considered prefix compressed only if it's valueOffset and valueLength are prefix compressed.
    override def isPrefixCompressed: Boolean = false

    override def read[V](indexReader: ReaderBase,
                         previous: Option[Persistent])(implicit valueOffsetReader: ValueOffsetReader[V],
                                                       valueLengthReader: ValueLengthReader[V]): Option[(Int, Int)] =
      ValueUncompressedReader.read(
        indexReader = indexReader,
        previous = previous
      )
  }
}
