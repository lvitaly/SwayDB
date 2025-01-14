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

package swaydb.core.segment.format.a.block.binarysearch

private[block] sealed trait BinarySearchGetResult[+T] {
  def toOption: Option[T]
  def toOptionApply[B](f: T => B): Option[B]
}

private[block] object BinarySearchGetResult {

  val none: BinarySearchGetResult.None[Nothing] =
    new BinarySearchGetResult.None(Option.empty[Nothing])

  class None[T](val lower: Option[T]) extends BinarySearchGetResult[T] {
    override val toOption: Option[T] = scala.None

    override def toOptionApply[B](f: T => B): Option[B] = scala.None
  }

  class Some[T](val value: T) extends BinarySearchGetResult[T] {
    override def toOption: Option[T] = scala.Some(value)

    override def toOptionApply[B](f: T => B): Option[B] = scala.Some(f(value))
  }
}
