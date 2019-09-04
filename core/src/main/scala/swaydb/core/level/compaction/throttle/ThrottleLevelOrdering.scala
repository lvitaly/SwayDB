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

package swaydb.core.level.compaction.throttle

import swaydb.core.level.zero.LevelZero
import swaydb.core.level.{Level, LevelRef, TrashLevel}

private[throttle] object ThrottleLevelOrdering {

  /**
   * Given the Level returns the ordering for [[LevelRef]].
   */
  def ordering(levelState: LevelRef => ThrottleLevelState) =
    new Ordering[LevelRef] {
      override def compare(left: LevelRef, right: LevelRef): Int = {
        (left, right) match {
          //Level
          case (left: Level, right: Level) => order(left, right, levelState(left), levelState(right))
          case (left: Level, right: LevelZero) => order(right, left, levelState(left), levelState(right)) * -1
          case (_: Level, TrashLevel) => 1
          //LevelZero
          case (left: LevelZero, right: Level) => order(left, right, levelState(left), levelState(right))
          case (_: LevelZero, _: LevelZero) => 0
          case (_: LevelZero, TrashLevel) => 1
          //LevelZero
          case (TrashLevel, _: Level) => -1
          case (TrashLevel, _: LevelZero) => -1
          case (TrashLevel, TrashLevel) => 0
        }
      }
    }

  def order(left: LevelZero,
            right: Level,
            leftState: ThrottleLevelState,
            rightState: ThrottleLevelState): Int =
    left.throttle(left.levelZeroMeter) compare right.throttle(right.meter).pushDelay

  def order(left: Level,
            right: Level,
            leftState: ThrottleLevelState,
            rightState: ThrottleLevelState): Int =
    left.throttle(left.meter).pushDelay compare right.throttle(right.meter).pushDelay
}
