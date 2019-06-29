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

package swaydb.data.config

import swaydb.data.api.grouping.Compression
import swaydb.data.config.BinarySearchIndex.{Disable, FullIndex, SecondaryIndex}

sealed trait BinarySearchIndex {
  def toOption: Option[BinarySearchIndex.Enable] =
    this match {
      case Disable => None
      case index: FullIndex => Some(index)
      case index: SecondaryIndex => Some(index)
    }
}

object BinarySearchIndex {
  case object Disable extends BinarySearchIndex

  sealed trait Enable extends BinarySearchIndex {
    def minimumNumberOfKeys: Int
    def cacheOnRead: Boolean
    def compression: Seq[Compression]
  }
  case class FullIndex(minimumNumberOfKeys: Int,
                       cacheOnRead: Boolean,
                       compression: Seq[Compression]) extends Enable

  case class SecondaryIndex(minimumNumberOfKeys: Int,
                            cacheOnRead: Boolean,
                            compression: Seq[Compression]) extends Enable
}
