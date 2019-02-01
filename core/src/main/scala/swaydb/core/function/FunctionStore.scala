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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with SwayDB. If not, see <https://www.gnu.org/licenses/>.
 */

package swaydb.core.function

import java.util.concurrent.{ConcurrentHashMap, ConcurrentSkipListMap}

import swaydb.core.data.SwayFunction
import swaydb.data.order.KeyOrder
import swaydb.data.slice.Slice

trait FunctionStore {
  def get(functionId: Slice[Byte]): Option[SwayFunction]

  def put(functionId: Slice[Byte], function: SwayFunction): SwayFunction
}

object FunctionStore {

  def memory() =
    new MemoryStore()

}

class MemoryStore extends FunctionStore {

  private val functions = new ConcurrentHashMap[Slice[Byte], SwayFunction]()
  //  private val functions = new ConcurrentSkipListMap[Slice[Byte], SwayFunction](KeyOrder.default)

  override def get(functionId: Slice[Byte]): Option[SwayFunction] =
    Option(functions.get(functionId))

  override def put(functionId: Slice[Byte], function: SwayFunction): SwayFunction =
    functions.put(functionId, function)
}