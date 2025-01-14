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

package swaydb.core.segment.format.a

import swaydb.core.TestBase
import swaydb.core.cache.Cache
import swaydb.core.data.Persistent._
import swaydb.core.data.{Persistent, Time, Value}
import swaydb.core.segment.format.a.block.KeyMatcher.Result._
import swaydb.core.segment.format.a.block.{KeyMatcher, ValuesBlock}
import swaydb.data.order.KeyOrder
import swaydb.data.slice.Slice
import swaydb.serializers.Default._
import swaydb.serializers._

import scala.util.Random

class KeyMatcherSpec extends TestBase {

  implicit val integer = new KeyOrder[Slice[Byte]] {
    def compare(a: Slice[Byte], b: Slice[Byte]): Int =
      IntSerializer.read(a).compareTo(IntSerializer.read(b))
  }

  /**
   * These implicits are just to make it easier to read the test cases.
   * The tests are normally for the match to Key in the following array
   *
   * -1, 0, 1, 2
   *
   * Tests check for keys to match in all positions (before and after each key)
   */

  import swaydb.core.CommonAssertions.keyMatcherResultEquality

  val whichKeyValue = Random.shuffle((1 to 5).toList).head

  implicit def toFixed(int: Int): Persistent.Fixed =
    if (whichKeyValue == 1)
      Put(_key = int, None, null, Time.empty, 0, 0, 0, 0, 0, 0)
    else if (whichKeyValue == 2)
      Update(_key = int, None, null, Time.empty, 0, 0, 0, 0, 0, 0)
    else if (whichKeyValue == 3)
      Function(_key = int, null, Time.empty, 0, 0, 0, 0, 0, 0)
    else if (whichKeyValue == 4)
      PendingApply(_key = int, Time.empty, None, null, 0, 0, 0, 0, 0, 0)
    else
      Remove(_key = int, null, Time.empty, 0, 0, 0, 0)

  implicit def toSomeFixed(int: Int): Option[Persistent.Fixed] =
    Some(int)

  object RangeImplicits {

    /**
     * Convenience implicits to make it easier to read the test cases.
     * A tuple indicates a range's (fromKey, toKey)
     */

    val noneRangeValueCache =
      Cache.noIO[ValuesBlock.Offset, (Option[Value.FromValue], Value.RangeValue)](false, false, None) {
        (_, _) =>
          fail("")
      }

    implicit def toRange(tuple: (Int, Int)): Persistent.Range =
      Persistent.Range(_fromKey = tuple._1, _toKey = tuple._2, valueCache = noneRangeValueCache, 0, 0, 0, 0, 0, 0)

    implicit def toSomeRange(tuple: (Int, Int)): Option[Persistent.Range] =
      Some(tuple)
  }

  "KeyMatcher" should {
    //    "shouldFetchNext" should {
    //      "return false" when {
    //        "it's matchOnly" in {
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Get.MatchOnly(1), Some(2)) shouldBe false
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Higher.MatchOnly(1), Some(2)) shouldBe false
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Lower.MatchOnly(1), Some(2)) shouldBe false
    //
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Get.MatchOnly(1), None) shouldBe true
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Higher.MatchOnly(1), None) shouldBe true
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Lower.MatchOnly(1), None) shouldBe true
    //
    //          import RangeImplicits._
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Get.MatchOnly(1), Some((2, 3))) shouldBe false
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Higher.MatchOnly(1), Some((2, 3))) shouldBe false
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Lower.MatchOnly(1), Some((2, 3))) shouldBe false
    //
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Get.MatchOnly(1), None) shouldBe true
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Higher.MatchOnly(1), None) shouldBe true
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Lower.MatchOnly(1), None) shouldBe true
    //
    //          import GroupImplicits._
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Get.MatchOnly(1), Some((2, (3, 4)))) shouldBe false
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Higher.MatchOnly(1), Some((2, (3, 4)))) shouldBe false
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Lower.MatchOnly(1), Some((2, (3, 4)))) shouldBe false
    //
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Get.MatchOnly(1), None) shouldBe true
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Higher.MatchOnly(1), None) shouldBe true
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Lower.MatchOnly(1), None) shouldBe true
    //        }
    //
    //        "it's whilePrefixCompressed" in {
    //          isPrefixCompressed = false
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Get.WhilePrefixCompressed(1), Some(2)) shouldBe false
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Higher.WhilePrefixCompressed(1), Some(2)) shouldBe false
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Lower.WhilePrefixCompressed(1), Some(2)) shouldBe false
    //
    //          import RangeImplicits._
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Get.WhilePrefixCompressed(1), Some((2, 3))) shouldBe false
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Higher.WhilePrefixCompressed(1), Some((2, 3))) shouldBe false
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Lower.WhilePrefixCompressed(1), Some((2, 3))) shouldBe false
    //
    //          import GroupImplicits._
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Get.WhilePrefixCompressed(1), Some((2, (3, 4)))) shouldBe false
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Higher.WhilePrefixCompressed(1), Some((2, (3, 4)))) shouldBe false
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Lower.WhilePrefixCompressed(1), Some((2, (3, 4)))) shouldBe false
    //        }
    //      }
    //
    //      "return true" when {
    //        "it's WhilePrefixCompressed but next is None or next is isPrefixCompressed" in {
    //          isPrefixCompressed = true
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Get.WhilePrefixCompressed(1), eitherOne(None, Some(2))) shouldBe true
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Higher.WhilePrefixCompressed(1), eitherOne(None, Some(2))) shouldBe true
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Lower.WhilePrefixCompressed(1), eitherOne(None, Some(2))) shouldBe true
    //
    //          import RangeImplicits._
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Get.WhilePrefixCompressed(1), eitherOne(None, Some((2, 3)))) shouldBe true
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Higher.WhilePrefixCompressed(1), eitherOne(None, Some((2, 3)))) shouldBe true
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Lower.WhilePrefixCompressed(1), eitherOne(None, Some((2, 3)))) shouldBe true
    //
    //          import GroupImplicits._
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Get.WhilePrefixCompressed(1), eitherOne(None, Some((2, (3, 4))))) shouldBe true
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Higher.WhilePrefixCompressed(1), eitherOne(None, Some((2, (3, 4))))) shouldBe true
    //          KeyMatcher.shouldFetchNext(KeyMatcher.Lower.WhilePrefixCompressed(1), eitherOne(None, Some((2, (3, 4))))) shouldBe true
    //        }
    //      }
    //    }

    "Get" when {
      "Fixed" in {
        //-1
        //   0, 1, 2
        KeyMatcher.Get(-1).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Get(-1).apply(previous = 0, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Get(-1).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get(-1).apply(previous = 1, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get(-1).apply(previous = 0, next = 1, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get(-1).apply(previous = 0, next = 1, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get(-1).apply(previous = 0, next = 2, hasMore = false) shouldEqual new AheadOrNoneOrEnd(2)
        KeyMatcher.Get(-1).apply(previous = 0, next = 2, hasMore = true) shouldEqual new AheadOrNoneOrEnd(2)

        //0
        //0, 1, 2
        KeyMatcher.Get(0).apply(previous = 0, next = None, hasMore = false) shouldEqual new Matched(None, 0, None)
        KeyMatcher.Get(0).apply(previous = 0, next = None, hasMore = true) shouldEqual new Matched(None, 0, None)
        KeyMatcher.Get(0).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get(0).apply(previous = 1, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        //next should never be fetched if previous was a match. This should not occur in actual scenarios.
        //        KeyMatcher.Get(0).apply(previous = 0, next = 1, hasMore = false) shouldEqual new Matched(0)
        //        KeyMatcher.Get(0).apply(previous = 0, next = 1, hasMore = true) shouldEqual new Matched(0)
        //        KeyMatcher.Get(0).apply(previous = 0, next = 2, hasMore = false) shouldEqual new Matched(0)
        //        KeyMatcher.Get(0).apply(previous = 0, next = 2, hasMore = true) shouldEqual new Matched(0)

        //   1
        //0, 1, 2
        KeyMatcher.Get(1).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Get(1).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindFetchNext(0)
        KeyMatcher.Get(1).apply(previous = 1, next = None, hasMore = false) shouldEqual new Matched(None, 1, None)
        KeyMatcher.Get(1).apply(previous = 1, next = None, hasMore = true) shouldEqual new Matched(None, 1, None)
        KeyMatcher.Get(1).apply(previous = 0, next = 1, hasMore = false) shouldEqual new Matched(0, 1, None)
        KeyMatcher.Get(1).apply(previous = 0, next = 1, hasMore = true) shouldEqual new Matched(0, 1, None)
        KeyMatcher.Get(1).apply(previous = 0, next = 2, hasMore = false) shouldEqual new AheadOrNoneOrEnd(2)
        KeyMatcher.Get(1).apply(previous = 0, next = 2, hasMore = true) shouldEqual new AheadOrNoneOrEnd(2)

        //      2
        //0, 1, 2
        KeyMatcher.Get(2).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Get(2).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindFetchNext(0)
        KeyMatcher.Get(2).apply(previous = 0, next = 1, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get(2).apply(previous = 0, next = 1, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Get(2).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get(2).apply(previous = 1, next = None, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Get(2).apply(previous = 1, next = 2, hasMore = false) shouldEqual new Matched(1, 2, None)
        KeyMatcher.Get(2).apply(previous = 1, next = 2, hasMore = true) shouldEqual new Matched(1, 2, None)
        KeyMatcher.Get(2).apply(previous = 2, next = None, hasMore = false) shouldEqual new Matched(None, 2, None)
        KeyMatcher.Get(2).apply(previous = 2, next = None, hasMore = true) shouldEqual new Matched(None, 2, None)

        //         3
        //0, 1, 2
        KeyMatcher.Get(3).apply(previous = 2, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(2)
        KeyMatcher.Get(3).apply(previous = 2, next = None, hasMore = true) shouldEqual new BehindFetchNext(2)
        KeyMatcher.Get(3).apply(previous = 3, next = None, hasMore = false) shouldEqual new Matched(None, 3, None)
        KeyMatcher.Get(3).apply(previous = 3, next = None, hasMore = true) shouldEqual new Matched(None, 3, None)
        KeyMatcher.Get(3).apply(previous = 2, next = 3, hasMore = false) shouldEqual new Matched(2, 3, None)
        KeyMatcher.Get(3).apply(previous = 2, next = 3, hasMore = true) shouldEqual new Matched(2, 3, None)
        KeyMatcher.Get(3).apply(previous = 2, next = 4, hasMore = false) shouldEqual new AheadOrNoneOrEnd(4)
        KeyMatcher.Get(3).apply(previous = 2, next = 4, hasMore = true) shouldEqual new AheadOrNoneOrEnd(4)
        KeyMatcher.Get(3).apply(previous = 0, next = 4, hasMore = true) shouldEqual new AheadOrNoneOrEnd(4)
      }

      //range tests
      "Range" in {
        import RangeImplicits._

        //-1
        KeyMatcher.Get(-1).apply(previous = 0, next = (5, 10), hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Get(-1).apply(previous = 0, next = (5, 10), hasMore = true) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Get(-1).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Get(-1).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Get(-1).apply(previous = (5, 10), next = 20, hasMore = false) shouldEqual new AheadOrNoneOrEnd(20)
        KeyMatcher.Get(-1).apply(previous = (5, 10), next = 20, hasMore = true) shouldEqual new AheadOrNoneOrEnd(20)

        KeyMatcher.Get(0).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Get(0).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Get(0).apply(previous = (5, 10), next = 20, hasMore = false) shouldEqual new AheadOrNoneOrEnd(20)
        KeyMatcher.Get(0).apply(previous = (5, 10), next = 20, hasMore = true) shouldEqual new AheadOrNoneOrEnd(20)

        KeyMatcher.Get(5).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Get(5).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Get(6).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Get(6).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Get(9).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Get(9).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Get(10).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new BehindFetchNext(5, 10)
        KeyMatcher.Get(10).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)

        KeyMatcher.Get(21).apply(previous = (5, 10), next = (20, 30), hasMore = false) shouldEqual new Matched((5, 10), (20, 30), None)
        KeyMatcher.Get(21).apply(previous = (5, 10), next = (20, 30), hasMore = true) shouldEqual new Matched((5, 10), (20, 30), None)
        KeyMatcher.Get(15).apply(previous = (5, 10), next = (20, 30), hasMore = true) shouldEqual new AheadOrNoneOrEnd(20, 30)
        KeyMatcher.Get(15).apply(previous = (5, 10), next = (20, 30), hasMore = false) shouldEqual new AheadOrNoneOrEnd(20, 30)
      }

      "up to the largest key and exit iteration early if the next key is larger then the key to find" in {

        def find(toFind: Int) =
          (1 to 100).foldLeft(0) {
            case (iterationCount, next) =>
              val result = KeyMatcher.Get(toFind).apply(previous = next, next = Some(next + 1), hasMore = true)
              if (next + 1 == toFind) {
                result shouldEqual new Matched(next, toFind, None)
                iterationCount + 1
              } else if (next + 1 > toFind) {
                result shouldBe a[AheadOrNoneOrEnd]
                iterationCount
              } else {
                result shouldEqual new BehindFetchNext(next + 1)
                iterationCount + 1
              }
          } shouldBe (toFind - 1)

        (1 to 100) foreach find
      }
    }

    "Get.MatchOnly" when {
      "Fixed" in {
        //-1
        //   0, 1, 2
        KeyMatcher.Get.MatchOnly(-1).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Get.MatchOnly(-1).apply(previous = 0, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Get.MatchOnly(-1).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get.MatchOnly(-1).apply(previous = 1, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get.MatchOnly(-1).apply(previous = 0, next = 1, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get.MatchOnly(-1).apply(previous = 0, next = 1, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get.MatchOnly(-1).apply(previous = 0, next = 2, hasMore = false) shouldEqual new AheadOrNoneOrEnd(2)
        KeyMatcher.Get.MatchOnly(-1).apply(previous = 0, next = 2, hasMore = true) shouldEqual new AheadOrNoneOrEnd(2)

        //0
        //0, 1, 2
        KeyMatcher.Get.MatchOnly(0).apply(previous = 0, next = None, hasMore = false) shouldEqual new Matched(None, 0, None)
        KeyMatcher.Get.MatchOnly(0).apply(previous = 0, next = None, hasMore = true) shouldEqual new Matched(None, 0, None)
        KeyMatcher.Get.MatchOnly(0).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get.MatchOnly(0).apply(previous = 1, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        //next should never be fetched if previous was a match. This should not occur in actual scenarios.
        //        KeyMatcher.Get.NextOnly(0).apply(previous = 0, next = 1, hasMore = false) shouldEqual new Matched(0)
        //        KeyMatcher.Get.NextOnly(0).apply(previous = 0, next = 1, hasMore = true) shouldEqual new Matched(0)
        //        KeyMatcher.Get.NextOnly(0).apply(previous = 0, next = 2, hasMore = false) shouldEqual new Matched(0)
        //        KeyMatcher.Get.NextOnly(0).apply(previous = 0, next = 2, hasMore = true) shouldEqual new Matched(0)

        //   1
        //0, 1, 2
        KeyMatcher.Get.MatchOnly(1).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Get.MatchOnly(1).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindStopped(0)
        KeyMatcher.Get.MatchOnly(1).apply(previous = 1, next = None, hasMore = false) shouldEqual new Matched(None, 1, None)
        KeyMatcher.Get.MatchOnly(1).apply(previous = 1, next = None, hasMore = true) shouldEqual new Matched(None, 1, None)
        KeyMatcher.Get.MatchOnly(1).apply(previous = 0, next = 1, hasMore = false) shouldEqual new Matched(0, 1, None)
        KeyMatcher.Get.MatchOnly(1).apply(previous = 0, next = 1, hasMore = true) shouldEqual new Matched(0, 1, None)
        KeyMatcher.Get.MatchOnly(1).apply(previous = 0, next = 2, hasMore = false) shouldEqual new AheadOrNoneOrEnd(2)
        KeyMatcher.Get.MatchOnly(1).apply(previous = 0, next = 2, hasMore = true) shouldEqual new AheadOrNoneOrEnd(2)

        //      2
        //0, 1, 2
        KeyMatcher.Get.MatchOnly(2).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Get.MatchOnly(2).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindStopped(0)
        KeyMatcher.Get.MatchOnly(2).apply(previous = 0, next = 1, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get.MatchOnly(2).apply(previous = 0, next = 1, hasMore = true) shouldEqual new BehindStopped(1)
        KeyMatcher.Get.MatchOnly(2).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Get.MatchOnly(2).apply(previous = 1, next = None, hasMore = true) shouldEqual new BehindStopped(1)
        KeyMatcher.Get.MatchOnly(2).apply(previous = 1, next = 2, hasMore = false) shouldEqual new Matched(1, 2, None)
        KeyMatcher.Get.MatchOnly(2).apply(previous = 1, next = 2, hasMore = true) shouldEqual new Matched(1, 2, None)
        KeyMatcher.Get.MatchOnly(2).apply(previous = 2, next = None, hasMore = false) shouldEqual new Matched(None, 2, None)
        KeyMatcher.Get.MatchOnly(2).apply(previous = 2, next = None, hasMore = true) shouldEqual new Matched(None, 2, None)

        //         3
        //0, 1, 2
        KeyMatcher.Get.MatchOnly(3).apply(previous = 2, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(2)
        KeyMatcher.Get.MatchOnly(3).apply(previous = 2, next = None, hasMore = true) shouldEqual new BehindStopped(2)
        KeyMatcher.Get.MatchOnly(3).apply(previous = 3, next = None, hasMore = false) shouldEqual new Matched(None, 3, None)
        KeyMatcher.Get.MatchOnly(3).apply(previous = 3, next = None, hasMore = true) shouldEqual new Matched(None, 3, None)
        KeyMatcher.Get.MatchOnly(3).apply(previous = 2, next = 3, hasMore = false) shouldEqual new Matched(2, 3, None)
        KeyMatcher.Get.MatchOnly(3).apply(previous = 2, next = 3, hasMore = true) shouldEqual new Matched(2, 3, None)
        KeyMatcher.Get.MatchOnly(3).apply(previous = 2, next = 4, hasMore = false) shouldEqual new AheadOrNoneOrEnd(4)
        KeyMatcher.Get.MatchOnly(3).apply(previous = 2, next = 4, hasMore = true) shouldEqual new AheadOrNoneOrEnd(4)
        KeyMatcher.Get.MatchOnly(3).apply(previous = 0, next = 4, hasMore = true) shouldEqual new AheadOrNoneOrEnd(4)
      }

      //range tests
      "Range" in {
        import RangeImplicits._

        //-1
        KeyMatcher.Get.MatchOnly(-1).apply(previous = 0, next = (5, 10), hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Get.MatchOnly(-1).apply(previous = 0, next = (5, 10), hasMore = true) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Get.MatchOnly(-1).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Get.MatchOnly(-1).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Get.MatchOnly(-1).apply(previous = (5, 10), next = 20, hasMore = false) shouldEqual new AheadOrNoneOrEnd(20)
        KeyMatcher.Get.MatchOnly(-1).apply(previous = (5, 10), next = 20, hasMore = true) shouldEqual new AheadOrNoneOrEnd(20)

        KeyMatcher.Get.MatchOnly(0).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Get.MatchOnly(0).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Get.MatchOnly(0).apply(previous = (5, 10), next = 20, hasMore = false) shouldEqual new AheadOrNoneOrEnd(20)
        KeyMatcher.Get.MatchOnly(0).apply(previous = (5, 10), next = 20, hasMore = true) shouldEqual new AheadOrNoneOrEnd(20)

        KeyMatcher.Get.MatchOnly(5).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Get.MatchOnly(5).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Get.MatchOnly(6).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Get.MatchOnly(6).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Get.MatchOnly(9).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Get.MatchOnly(9).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Get.MatchOnly(10).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new BehindStopped(5, 10)
        KeyMatcher.Get.MatchOnly(10).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)

        KeyMatcher.Get.MatchOnly(21).apply(previous = (5, 10), next = (20, 30), hasMore = false) shouldEqual new Matched((5, 10), (20, 30), None)
        KeyMatcher.Get.MatchOnly(21).apply(previous = (5, 10), next = (20, 30), hasMore = true) shouldEqual new Matched((5, 10), (20, 30), None)
        KeyMatcher.Get.MatchOnly(15).apply(previous = (5, 10), next = (20, 30), hasMore = true) shouldEqual new AheadOrNoneOrEnd(20, 30)
        KeyMatcher.Get.MatchOnly(15).apply(previous = (5, 10), next = (20, 30), hasMore = false) shouldEqual new AheadOrNoneOrEnd(20, 30)
      }

      "up to the largest key and exit iteration early if the next key is larger then the key to find" in {

        def find(toFind: Int) =
          (1 to 100).foldLeft(0) {
            case (iterationCount, next) =>
              val result = KeyMatcher.Get(toFind).apply(previous = next, next = Some(next + 1), hasMore = true)
              if (next + 1 == toFind) {
                result shouldEqual new Matched(next, toFind, None)
                iterationCount + 1
              } else if (next + 1 > toFind) {
                result shouldBe a[AheadOrNoneOrEnd]
                iterationCount
              } else {
                result shouldEqual new BehindFetchNext(next + 1)
                iterationCount + 1
              }
          } shouldBe (toFind - 1)

        (1 to 100) foreach find
      }
    }

    "Lower" when {
      "Fixed" in {
        //0, 1, 2
        KeyMatcher.Lower(-1).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Lower(-1).apply(previous = 0, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Lower(-1).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower(-1).apply(previous = 1, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower(-1).apply(previous = 0, next = 1, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower(-1).apply(previous = 0, next = 1, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower(-1).apply(previous = 0, next = 2, hasMore = false) shouldEqual new AheadOrNoneOrEnd(2)
        KeyMatcher.Lower(-1).apply(previous = 0, next = 2, hasMore = true) shouldEqual new AheadOrNoneOrEnd(2)

        KeyMatcher.Lower(0).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Lower(0).apply(previous = 0, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Lower(0).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower(0).apply(previous = 1, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower(0).apply(previous = 0, next = 1, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower(0).apply(previous = 0, next = 1, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower(0).apply(previous = 0, next = 2, hasMore = false) shouldEqual new AheadOrNoneOrEnd(2)
        KeyMatcher.Lower(0).apply(previous = 0, next = 2, hasMore = true) shouldEqual new AheadOrNoneOrEnd(2)

        KeyMatcher.Lower(1).apply(previous = 0, next = None, hasMore = false) shouldEqual new Matched(None, 0, None)
        KeyMatcher.Lower(1).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindFetchNext(0)
        KeyMatcher.Lower(1).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower(1).apply(previous = 1, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower(1).apply(previous = 0, next = 1, hasMore = false) shouldEqual new Matched(None, 0, 1)
        KeyMatcher.Lower(1).apply(previous = 0, next = 1, hasMore = true) shouldEqual new Matched(None, 0, 1)
        KeyMatcher.Lower(1).apply(previous = 0, next = 2, hasMore = false) shouldEqual new Matched(None, 0, 2)
        KeyMatcher.Lower(1).apply(previous = 0, next = 2, hasMore = true) shouldEqual new Matched(None, 0, 2)

        KeyMatcher.Lower(2).apply(previous = 0, next = None, hasMore = false) shouldEqual new Matched(None, 0, None)
        KeyMatcher.Lower(2).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindFetchNext(0)
        KeyMatcher.Lower(2).apply(previous = 1, next = None, hasMore = false) shouldEqual new Matched(None, 1, None)
        KeyMatcher.Lower(2).apply(previous = 1, next = None, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Lower(2).apply(previous = 0, next = 1, hasMore = false) shouldEqual new Matched(0, 1, None)
        KeyMatcher.Lower(2).apply(previous = 0, next = 1, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Lower(2).apply(previous = 0, next = 2, hasMore = false) shouldEqual new Matched(None, 0, 2)
        KeyMatcher.Lower(2).apply(previous = 0, next = 2, hasMore = true) shouldEqual new Matched(None, 0, 2)

        KeyMatcher.Lower(3).apply(previous = 0, next = None, hasMore = false) shouldEqual new Matched(None, 0, None)
        KeyMatcher.Lower(3).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindFetchNext(0)
        KeyMatcher.Lower(3).apply(previous = 1, next = None, hasMore = false) shouldEqual new Matched(None, 1, None)
        KeyMatcher.Lower(3).apply(previous = 1, next = None, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Lower(3).apply(previous = 0, next = 1, hasMore = false) shouldEqual new Matched(0, 1, None)
        KeyMatcher.Lower(3).apply(previous = 0, next = 1, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Lower(3).apply(previous = 0, next = 2, hasMore = false) shouldEqual new Matched(0, 2, None)
        KeyMatcher.Lower(3).apply(previous = 0, next = 2, hasMore = true) shouldEqual new BehindFetchNext(2)
      }

      "Range" in {

        import RangeImplicits._

        KeyMatcher.Lower(3).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Lower(3).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Lower(4).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Lower(4).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Lower(5).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Lower(5).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(5, 10)

        KeyMatcher.Lower(6).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Lower(6).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Lower(10).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Lower(10).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)

        KeyMatcher.Lower(11).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Lower(11).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new BehindFetchNext(5, 10)

        KeyMatcher.Lower(12).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new Matched(None, (5, 10), (15, 20))
        KeyMatcher.Lower(12).apply(previous = (5, 10), next = (15, 20), hasMore = false) shouldEqual new Matched(None, (5, 10), (15, 20))
        KeyMatcher.Lower(15).apply(previous = (5, 10), next = (15, 20), hasMore = false) shouldEqual new Matched(None, (5, 10), (15, 20))
        KeyMatcher.Lower(15).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new Matched(None, (5, 10), (15, 20))

        KeyMatcher.Lower(20).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Lower(20).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new BehindFetchNext(5, 10)
        KeyMatcher.Lower(20).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new Matched((5, 10), (15, 20), None)
        KeyMatcher.Lower(20).apply(previous = (5, 10), next = (15, 20), hasMore = false) shouldEqual new Matched((5, 10), (15, 20), None)
        KeyMatcher.Lower(20).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new Matched((5, 10), (15, 20), None)

        KeyMatcher.Lower(21).apply(previous = (5, 10), next = (15, 20), hasMore = false) shouldEqual new Matched((5, 10), (15, 20), None)
        KeyMatcher.Lower(21).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new BehindFetchNext(15, 20)
      }

      "minimum number of lower keys to fulfil the request" in {

        def find(toFind: Int) =
          (1 to 100).foldLeft(0) {
            case (iterationCount, next) =>
              val result = KeyMatcher.Lower(toFind).apply(previous = next, next = Some(next + 1), hasMore = true)
              if (next + 1 == toFind) {
                result shouldEqual new Matched(None, toFind - 1, toFind)
                iterationCount + 1
              } else if (next + 1 > toFind) {
                result shouldBe a[AheadOrNoneOrEnd]
                iterationCount
              } else {
                result shouldEqual new BehindFetchNext(next + 1)
                iterationCount + 1
              }
          } shouldBe (toFind - 1)

        (1 to 100) foreach find
      }
    }

    "Lower.MatchOnly" when {
      "Fixed" in {
        //0, 1, 2
        KeyMatcher.Lower.MatchOnly(-1).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Lower.MatchOnly(-1).apply(previous = 0, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Lower.MatchOnly(-1).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower.MatchOnly(-1).apply(previous = 1, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower.MatchOnly(-1).apply(previous = 0, next = 1, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower.MatchOnly(-1).apply(previous = 0, next = 1, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower.MatchOnly(-1).apply(previous = 0, next = 2, hasMore = false) shouldEqual new AheadOrNoneOrEnd(2)
        KeyMatcher.Lower.MatchOnly(-1).apply(previous = 0, next = 2, hasMore = true) shouldEqual new AheadOrNoneOrEnd(2)

        KeyMatcher.Lower.MatchOnly(0).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Lower.MatchOnly(0).apply(previous = 0, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Lower.MatchOnly(0).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower.MatchOnly(0).apply(previous = 1, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower.MatchOnly(0).apply(previous = 0, next = 1, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower.MatchOnly(0).apply(previous = 0, next = 1, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower.MatchOnly(0).apply(previous = 0, next = 2, hasMore = false) shouldEqual new AheadOrNoneOrEnd(2)
        KeyMatcher.Lower.MatchOnly(0).apply(previous = 0, next = 2, hasMore = true) shouldEqual new AheadOrNoneOrEnd(2)

        KeyMatcher.Lower.MatchOnly(1).apply(previous = 0, next = None, hasMore = false) shouldEqual new Matched(None, 0, None)
        KeyMatcher.Lower.MatchOnly(1).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindFetchNext(0)
        KeyMatcher.Lower.MatchOnly(1).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower.MatchOnly(1).apply(previous = 1, next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Lower.MatchOnly(1).apply(previous = 0, next = 1, hasMore = false) shouldEqual new Matched(None, 0, 1)
        KeyMatcher.Lower.MatchOnly(1).apply(previous = 0, next = 1, hasMore = true) shouldEqual new Matched(None, 0, 1)
        KeyMatcher.Lower.MatchOnly(1).apply(previous = 0, next = 2, hasMore = false) shouldEqual new Matched(None, 0, 2)
        KeyMatcher.Lower.MatchOnly(1).apply(previous = 0, next = 2, hasMore = true) shouldEqual new Matched(None, 0, 2)

        KeyMatcher.Lower.MatchOnly(2).apply(previous = 0, next = None, hasMore = false) shouldEqual new Matched(None, 0, None)
        KeyMatcher.Lower.MatchOnly(2).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindFetchNext(0)
        KeyMatcher.Lower.MatchOnly(2).apply(previous = 1, next = None, hasMore = false) shouldEqual new Matched(None, 1, None)
        KeyMatcher.Lower.MatchOnly(2).apply(previous = 1, next = None, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Lower.MatchOnly(2).apply(previous = 0, next = 1, hasMore = false) shouldEqual new Matched(0, 1, None)
        KeyMatcher.Lower.MatchOnly(2).apply(previous = 0, next = 1, hasMore = true) shouldEqual new BehindStopped(1)
        KeyMatcher.Lower.MatchOnly(2).apply(previous = 0, next = 2, hasMore = false) shouldEqual new Matched(None, 0, 2)
        KeyMatcher.Lower.MatchOnly(2).apply(previous = 0, next = 2, hasMore = true) shouldEqual new Matched(None, 0, 2)

        KeyMatcher.Lower.MatchOnly(3).apply(previous = 0, next = None, hasMore = false) shouldEqual new Matched(None, 0, None)
        KeyMatcher.Lower.MatchOnly(3).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindFetchNext(0)
        KeyMatcher.Lower.MatchOnly(3).apply(previous = 1, next = None, hasMore = false) shouldEqual new Matched(None, 1, None)
        KeyMatcher.Lower.MatchOnly(3).apply(previous = 1, next = None, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Lower.MatchOnly(3).apply(previous = 0, next = 1, hasMore = false) shouldEqual new Matched(0, 1, None)
        KeyMatcher.Lower.MatchOnly(3).apply(previous = 0, next = 1, hasMore = true) shouldEqual new BehindStopped(1)
        KeyMatcher.Lower.MatchOnly(3).apply(previous = 0, next = 2, hasMore = false) shouldEqual new Matched(0, 2, None)
        KeyMatcher.Lower.MatchOnly(3).apply(previous = 0, next = 2, hasMore = true) shouldEqual new BehindStopped(2)
      }

      "Range" in {

        import RangeImplicits._

        KeyMatcher.Lower.MatchOnly(3).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Lower.MatchOnly(3).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Lower.MatchOnly(4).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Lower.MatchOnly(4).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Lower.MatchOnly(5).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Lower.MatchOnly(5).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new AheadOrNoneOrEnd(5, 10)

        KeyMatcher.Lower.MatchOnly(6).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Lower.MatchOnly(6).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Lower.MatchOnly(10).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Lower.MatchOnly(10).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)

        KeyMatcher.Lower.MatchOnly(11).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Lower.MatchOnly(11).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new BehindFetchNext(5, 10)

        KeyMatcher.Lower.MatchOnly(12).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new Matched(None, (5, 10), (15, 20))
        KeyMatcher.Lower.MatchOnly(12).apply(previous = (5, 10), next = (15, 20), hasMore = false) shouldEqual new Matched(None, (5, 10), (15, 20))
        KeyMatcher.Lower.MatchOnly(15).apply(previous = (5, 10), next = (15, 20), hasMore = false) shouldEqual new Matched(None, (5, 10), (15, 20))
        KeyMatcher.Lower.MatchOnly(15).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new Matched(None, (5, 10), (15, 20))

        KeyMatcher.Lower.MatchOnly(20).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Lower.MatchOnly(20).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new BehindFetchNext(5, 10)
        KeyMatcher.Lower.MatchOnly(20).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new Matched((5, 10), (15, 20), None)
        KeyMatcher.Lower.MatchOnly(20).apply(previous = (5, 10), next = (15, 20), hasMore = false) shouldEqual new Matched((5, 10), (15, 20), None)
        KeyMatcher.Lower.MatchOnly(20).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new Matched((5, 10), (15, 20), None)

        KeyMatcher.Lower.MatchOnly(21).apply(previous = (5, 10), next = (15, 20), hasMore = false) shouldEqual new Matched((5, 10), (15, 20), None)
        KeyMatcher.Lower.MatchOnly(21).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new BehindStopped(15, 20)
      }
    }

    "Higher" when {

      "Fixed" in {
        //0, 1, 2
        KeyMatcher.Higher(-1).apply(previous = 0, next = None, hasMore = false) shouldEqual new Matched(None, 0, None)
        KeyMatcher.Higher(-1).apply(previous = 0, next = None, hasMore = true) shouldEqual new Matched(None, 0, None)
        KeyMatcher.Higher(-1).apply(previous = 1, next = None, hasMore = false) shouldEqual new Matched(None, 1, None)
        KeyMatcher.Higher(-1).apply(previous = 1, next = None, hasMore = true) shouldEqual new Matched(None, 1, None)
        //    KeyMatcher.Higher(-1).apply[CreatedReadOnly](previous = 0, next = 1, hasMore = false) shouldBe AheadOrEnd
        //    KeyMatcher.Higher(-1).apply[CreatedReadOnly](previous = 0, next = 1, hasMore = true) shouldBe AheadOrEnd
        //    KeyMatcher.Higher(-1).apply[CreatedReadOnly](previous = 0, next = 2, hasMore = false) shouldBe AheadOrEnd
        //    KeyMatcher.Higher(-1).apply[CreatedReadOnly](previous = 0, next = 2, hasMore = true) shouldBe AheadOrEnd

        KeyMatcher.Higher(0).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Higher(0).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindFetchNext(0)
        KeyMatcher.Higher(0).apply(previous = 1, next = None, hasMore = false) shouldEqual new Matched(None, 1, None)
        KeyMatcher.Higher(0).apply(previous = 1, next = None, hasMore = true) shouldEqual new Matched(None, 1, None)
        KeyMatcher.Higher(0).apply(previous = 0, next = 1, hasMore = false) shouldEqual new Matched(0, 1, None)
        KeyMatcher.Higher(0).apply(previous = 0, next = 1, hasMore = true) shouldEqual new Matched(0, 1, None)
        KeyMatcher.Higher(0).apply(previous = 0, next = 2, hasMore = false) shouldEqual new Matched(0, 2, None)
        KeyMatcher.Higher(0).apply(previous = 0, next = 2, hasMore = true) shouldEqual new Matched(0, 2, None)

        KeyMatcher.Higher(1).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Higher(1).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindFetchNext(0)
        KeyMatcher.Higher(1).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Higher(1).apply(previous = 1, next = None, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Higher(1).apply(previous = 0, next = 1, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Higher(1).apply(previous = 0, next = 1, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Higher(1).apply(previous = 0, next = 2, hasMore = false) shouldEqual new Matched(0, 2, None)
        KeyMatcher.Higher(1).apply(previous = 0, next = 2, hasMore = true) shouldEqual new Matched(0, 2, None)

        KeyMatcher.Higher(2).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Higher(2).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindFetchNext(0)
        KeyMatcher.Higher(2).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Higher(2).apply(previous = 1, next = None, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Higher(2).apply(previous = 0, next = 1, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Higher(2).apply(previous = 0, next = 1, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Higher(2).apply(previous = 0, next = 2, hasMore = false) shouldEqual new AheadOrNoneOrEnd(2)
        KeyMatcher.Higher(2).apply(previous = 0, next = 2, hasMore = true) shouldEqual new BehindFetchNext(2)

        KeyMatcher.Higher(3).apply(previous = 0, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(0)
        KeyMatcher.Higher(3).apply(previous = 0, next = None, hasMore = true) shouldEqual new BehindFetchNext(0)
        KeyMatcher.Higher(3).apply(previous = 1, next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Higher(3).apply(previous = 1, next = None, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Higher(3).apply(previous = 0, next = 1, hasMore = false) shouldEqual new AheadOrNoneOrEnd(1)
        KeyMatcher.Higher(3).apply(previous = 0, next = 1, hasMore = true) shouldEqual new BehindFetchNext(1)
        KeyMatcher.Higher(3).apply(previous = 0, next = 2, hasMore = false) shouldEqual new AheadOrNoneOrEnd(2)
        KeyMatcher.Higher(3).apply(previous = 0, next = 2, hasMore = true) shouldEqual new BehindFetchNext(2)
      }

      "Range" in {
        import RangeImplicits._

        KeyMatcher.Higher(3).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Higher(3).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Higher(4).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Higher(4).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Higher(5).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Higher(5).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)

        KeyMatcher.Higher(6).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Higher(6).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new Matched(None, (5, 10), None)
        KeyMatcher.Higher(10).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new BehindFetchNext(5, 10)
        KeyMatcher.Higher(10).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)

        KeyMatcher.Higher(11).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Higher(11).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new BehindFetchNext(5, 10)

        KeyMatcher.Higher(12).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new Matched((5, 10), (15, 20), None)
        KeyMatcher.Higher(12).apply(previous = (5, 10), next = (15, 20), hasMore = false) shouldEqual new Matched((5, 10), (15, 20), None)
        KeyMatcher.Higher(15).apply(previous = (5, 10), next = (15, 20), hasMore = false) shouldEqual new Matched((5, 10), (15, 20), None)
        KeyMatcher.Higher(15).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new Matched((5, 10), (15, 20), None)

        KeyMatcher.Higher(19).apply(previous = (5, 10), next = (15, 20), hasMore = false) shouldEqual new Matched((5, 10), (15, 20), None)
        KeyMatcher.Higher(19).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new Matched((5, 10), (15, 20), None)

        KeyMatcher.Higher(20).apply(previous = (5, 10), next = None, hasMore = false) shouldEqual new AheadOrNoneOrEnd(5, 10)
        KeyMatcher.Higher(20).apply(previous = (5, 10), next = None, hasMore = true) shouldEqual new BehindFetchNext(5, 10)
        KeyMatcher.Higher(20).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new BehindFetchNext(15, 20)
        KeyMatcher.Higher(20).apply(previous = (5, 10), next = (15, 20), hasMore = false) shouldEqual new AheadOrNoneOrEnd(15, 20)
        KeyMatcher.Higher(20).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new BehindFetchNext(15, 20)

        KeyMatcher.Higher(21).apply(previous = (5, 10), next = (15, 20), hasMore = false) shouldEqual new AheadOrNoneOrEnd(15, 20)
        KeyMatcher.Higher(21).apply(previous = (5, 10), next = (15, 20), hasMore = true) shouldEqual new BehindFetchNext(15, 20)
      }
    }
  }
}
