/*
 * Copyright (C) 2018 Simer Plaha (@simerplaha)
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with SwayDB. If not, see <https://www.gnu.org/licenses/>.
 */

package swaydb.core.data

import swaydb.core.data.KeyValue.{KeyValueTuple, KeyValueInternal, ValuePair}
import swaydb.core.data.Transient.Put
import swaydb.data.slice.Slice

import scala.util.{Success, Try}

private[core] sealed trait KeyValueType {
  def key: Slice[Byte]

  def id: Int

  def isRemove: Boolean

  def notDelete: Boolean = !isRemove

  def keyLength =
    key.size

  def getOrFetchValue: Try[Option[Slice[Byte]]]

  def toKeyValuePair: Try[Option[KeyValueInternal]] =
    getOrFetchValue map {
      value =>
        val valueType = if (isRemove) ValueType.Remove else ValueType.Add
        Some(key, (valueType, value))
    }

  def toValueResponse: Try[Option[ValuePair]] =
    getOrFetchValue map {
      value =>
        val valueType = if (isRemove) ValueType.Remove else ValueType.Add
        Some(valueType, value)
    }

  def toTuple: Try[Option[KeyValueTuple]] =
    getOrFetchValue map {
      value =>
        Some(key, value)
    }
}

trait KeyValueReadOnly extends KeyValueType {
  val valueLength: Int
}

trait KeyValue extends KeyValueType {
  val stats: Stats

  def updateStats(falsePositiveRate: Double, keyValue: Option[KeyValue]): KeyValue

}

private[core] object KeyValue {

  type KeyValueTuple = (Slice[Byte], Option[Slice[Byte]])

  implicit class ToKeyValueTypeFromInternal(keyVal: KeyValueInternal) {
    def toKeyValueType = new KeyValueType {
      override def isRemove: Boolean =
        keyVal._2._1.isDelete

      override def getOrFetchValue: Try[Option[Slice[Byte]]] =
        Success(keyVal._2._2)

      override def id: Int =
        keyVal._2._1.id

      override def key: Slice[Byte] =
        keyVal._1
    }
  }

  type KeyValueInternal = (Slice[Byte], (ValueType, Option[Slice[Byte]]))

  type ValuePair = (ValueType, Option[Slice[Byte]])

}