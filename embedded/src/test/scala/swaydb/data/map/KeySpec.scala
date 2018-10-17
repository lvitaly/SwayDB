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

package swaydb.data.map

import swaydb.TestBaseEmbedded
import swaydb.data.slice.Slice
import swaydb.extensions.Key
import swaydb.serializers.Default._
import swaydb.serializers.Serializer

import scala.collection.SortedSet
import scala.util.Random

class KeySpec extends TestBaseEmbedded {

  override val keyValueCount: Int = 100

  "mapKeySerializer" should {
    def doAssert[T](key: Key[T])(implicit serializer: Serializer[T]) = {
      val mapKeySerializer = Key.serializer[T](serializer)
      val wrote = mapKeySerializer.write(key)
      val read = mapKeySerializer.read(wrote)
      read shouldBe key
    }

    "write & read MapKeys with Int key" in {
      doAssert(Key.Start(Seq(1)))
      doAssert(Key.EntriesStart(Seq(1)))
      doAssert(Key.Entry(Seq(1), 100))
      doAssert(Key.EntriesEnd(Seq(1)))
      doAssert(Key.SubMapsStart(Seq(1)))
      doAssert(Key.SubMap(Seq(1), 1000))
      doAssert(Key.SubMapsEnd(Seq(1)))
      doAssert(Key.End(Seq(1)))
    }

    "write & read MapKeys with multiple Int keys" in {
      doAssert(Key.Start(Seq(1, 2, 3)))
      doAssert(Key.EntriesStart(Seq(1, 2, 3)))
      doAssert(Key.Entry(Seq(1, 2, 3), 100))
      doAssert(Key.EntriesEnd(Seq(1, 2, 3)))
      doAssert(Key.SubMapsStart(Seq(1, 2, 3)))
      doAssert(Key.SubMap(Seq(1, 2, 3), 1000))
      doAssert(Key.SubMapsEnd(Seq(1, 2, 3)))
      doAssert(Key.End(Seq(1, 2, 3)))
    }

    "write & read MapKeys with Int String" in {
      doAssert(Key.Start(Seq("one")))
      doAssert(Key.EntriesStart(Seq("one")))
      doAssert(Key.Entry(Seq("one"), "one key"))
      doAssert(Key.EntriesEnd(Seq("one")))
      doAssert(Key.SubMapsStart(Seq("one")))
      doAssert(Key.SubMap(Seq("one"), "one sub map"))
      doAssert(Key.SubMapsEnd(Seq("one")))
      doAssert(Key.End(Seq("one")))
    }

    "write & read MapKeys with large single value" in {
      doAssert(Key.Start(Seq(randomCharacters(100000))))
      doAssert(Key.EntriesStart(Seq(randomCharacters(100000))))
      doAssert(Key.EntriesEnd(Seq(randomCharacters(100000))))
      doAssert(Key.Entry(Seq(randomCharacters(100000)), randomCharacters(100000)))
      doAssert(Key.SubMapsStart(Seq(randomCharacters(100000))))
      doAssert(Key.SubMap(Seq(randomCharacters(100000)), randomCharacters(100000)))
      doAssert(Key.SubMapsEnd(Seq(randomCharacters(100000))))
      doAssert(Key.End(Seq(randomCharacters(100000))))
    }

    "write & read MapKeys with Double" in {
      doAssert(Key.Start(Seq(Double.MinValue)))
      doAssert(Key.EntriesStart(Seq(Double.MinValue)))
      doAssert(Key.Entry(Seq(Double.MinValue), Double.MaxValue))
      doAssert(Key.EntriesEnd(Seq(Double.MinValue)))
      doAssert(Key.SubMapsStart(Seq(Double.MinValue)))
      doAssert(Key.SubMap(Seq(Double.MinValue), Double.MaxValue))
      doAssert(Key.SubMapsEnd(Seq(Double.MinValue)))
      doAssert(Key.End(Seq(Double.MinValue)))
    }

  }

  "ordering" should {
    "ordering MapKeys in the order of Start, Entry & End" in {
      val order = Ordering.by[Slice[Byte], Int](_.readInt())(Ordering.Int)
      val mapKeySerializer = Key.serializer[Int](IntSerializer)
      implicit val mapKeyOrder = Ordering.by[Key[Int], Slice[Byte]](mapKeySerializer.write)(Key.ordering(order))

      val keys = Seq(
        Key.Start(Seq(0)),
        Key.SubMapsStart(Seq(0)),
        Key.SubMapsEnd(Seq(0)),
        Key.End(Seq(0)),

        Key.Start(Seq(1)),
        Key.EntriesStart(Seq(1)),
        Key.Entry(Seq(1), 1),
        Key.EntriesEnd(Seq(1)),
        Key.SubMapsStart(Seq(1)),
        Key.SubMap(Seq(1), 1000),
        Key.SubMapsEnd(Seq(1)),
        Key.End(Seq(1)),

        Key.Start(Seq(100)),
        Key.EntriesStart(Seq(100)),
        Key.Entry(Seq(100), 2),
        Key.Entry(Seq(100), 3),
        Key.Entry(Seq(100), 4),
        Key.Entry(Seq(100), 5),
        Key.EntriesEnd(Seq(100)),
        Key.SubMapsStart(Seq(100)),
        Key.SubMap(Seq(100), 1000),
        Key.SubMap(Seq(100), 2000),
        Key.SubMap(Seq(100), 3000),
        Key.SubMapsEnd(Seq(100)),
        Key.End(Seq(100)),

        Key.Start(Seq(2, 3)),
        Key.EntriesStart(Seq(2, 3)),
        Key.Entry(Seq(2, 3), 2),
        Key.Entry(Seq(2, 3), 3),
        Key.EntriesEnd(Seq(2, 3)),
        Key.SubMapsStart(Seq(2, 3)),
        Key.SubMap(Seq(2, 3), 1000),
        Key.SubMap(Seq(2, 3), 2000),
        Key.SubMapsEnd(Seq(2, 3)),
        Key.End(Seq(2, 3))
      )

      //shuffle and create a list
      val map = SortedSet[Key[Int]](Random.shuffle(keys): _*)(mapKeyOrder)

      //key-values should
      map.toList shouldBe keys
    }

    "ordering MapKeys in the order of Start, Entry & End when keys are large String" in {
      val order = Ordering.by[Slice[Byte], String](_.readString())(Ordering.String)
      val mapKeySerializer = Key.serializer[String](StringSerializer)
      implicit val mapKeyOrder = Ordering.by[Key[String], Slice[Byte]](mapKeySerializer.write)(Key.ordering(order))

      val stringLength = 100000

      val randomString1 = "a" + randomCharacters(stringLength)
      val randomString2 = "b" + randomCharacters(stringLength)
      val randomString3 = "c" + randomCharacters(stringLength)
      val randomString4 = "d" + randomCharacters(stringLength)
      val randomString5 = "e" + randomCharacters(stringLength)

      val keys = Seq(
        Key.Start(Seq(randomString1)),
        Key.SubMapsStart(Seq(randomString1)),
        Key.SubMapsEnd(Seq(randomString1)),
        Key.End(Seq(randomString1)),

        Key.Start(Seq(randomString2)),
        Key.EntriesStart(Seq(randomString2)),
        Key.Entry(Seq(randomString2), randomString3),
        Key.Entry(Seq(randomString2), randomString4),
        Key.Entry(Seq(randomString2), randomString5),
        Key.EntriesEnd(Seq(randomString2)),
        Key.SubMapsStart(Seq(randomString2)),
        Key.SubMap(Seq(randomString2), randomString3),
        Key.SubMap(Seq(randomString2), randomString4),
        Key.SubMap(Seq(randomString2), randomString5),
        Key.SubMapsEnd(Seq(randomString2)),
        Key.End(Seq(randomString2)),

        Key.Start(Seq(randomString3)),
        Key.Entry(Seq(randomString3), randomString3),
        Key.Entry(Seq(randomString3), randomString4),
        Key.Entry(Seq(randomString3), randomString5),
        Key.End(Seq(randomString3))
      )

      //shuffle and create a list
      val map = SortedSet[Key[String]](Random.shuffle(keys): _*)(mapKeyOrder)

      //key-values should
      map.toList shouldBe keys
    }

    "remove duplicate key-values" in {
      val order = Ordering.by[Slice[Byte], Int](_.readInt())(Ordering.Int)
      val mapKeySerializer = Key.serializer[Int](IntSerializer)
      implicit val mapKeyOrder = Ordering.by[Key[Int], Slice[Byte]](mapKeySerializer.write)(Key.ordering(order))

      val keys = Seq(
        Key.Start(Seq(0)),
        Key.EntriesStart(Seq(0)),
        Key.EntriesEnd(Seq(0)),
        Key.End(Seq(0)),
        Key.Start(Seq(0)),
        Key.EntriesStart(Seq(0)),
        Key.EntriesEnd(Seq(0)),
        Key.End(Seq(0)),

        Key.Start(Seq(2)),
        Key.EntriesStart(Seq(2)),
        Key.Entry(Seq(2), 2),
        Key.Entry(Seq(2), 2),
        Key.EntriesEnd(Seq(2)),
        Key.SubMapsStart(Seq(2)),
        Key.SubMap(Seq(2), 1000),
        Key.SubMap(Seq(2), 1000),
        Key.SubMapsEnd(Seq(2)),
        Key.End(Seq(2)),

        Key.Start(Seq(100)),
        Key.Entry(Seq(100), 4),
        Key.Entry(Seq(100), 5),
        Key.End(Seq(100))
      )

      //shuffle and create a list
      val map = SortedSet[Key[Int]](Random.shuffle(keys): _*)(mapKeyOrder)

      val expected = Seq(
        Key.Start(Seq(0)),
        Key.EntriesStart(Seq(0)),
        Key.EntriesEnd(Seq(0)),
        Key.End(Seq(0)),

        Key.Start(Seq(2)),
        Key.EntriesStart(Seq(2)),
        Key.Entry(Seq(2), 2),
        Key.EntriesEnd(Seq(2)),
        Key.SubMapsStart(Seq(2)),
        Key.SubMap(Seq(2), 1000),
        Key.SubMapsEnd(Seq(2)),
        Key.End(Seq(2)),

        Key.Start(Seq(100)),
        Key.Entry(Seq(100), 4),
        Key.Entry(Seq(100), 5),
        Key.End(Seq(100))
      )

      //key-values should
      map.toList shouldBe expected
    }

  }
}