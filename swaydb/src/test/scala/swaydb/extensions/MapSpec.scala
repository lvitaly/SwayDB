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

package swaydb.extensions

import org.scalatest.OptionValues._
import swaydb.IOValues._
import swaydb.Prepare
import swaydb.api.TestBaseEmbedded
import swaydb.core.CommonAssertions._
import swaydb.core.RunThis._
import swaydb.data.order.KeyOrder
import swaydb.data.slice.Slice
import swaydb.data.util.StorageUnits._
import swaydb.serializers.Default._

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class MapSpec0 extends MapSpec {
  val keyValueCount: Int = 1000

  override def newDB(): Map[Int, String] =
    swaydb.extensions.persistent.Map[Int, String, Nothing](dir = randomDir).runRandomIO.right.value.runRandomIO.right.value
}

class MapSpec1 extends MapSpec {

  val keyValueCount: Int = 10000

  override def newDB(): Map[Int, String] =
    swaydb.extensions.persistent.Map[Int, String, Nothing](randomDir, mapSize = 1.byte).runRandomIO.right.value.runRandomIO.right.value
}

class MapSpec2 extends MapSpec {

  val keyValueCount: Int = 100000

  override def newDB(): Map[Int, String] =
    swaydb.extensions.memory.Map[Int, String, Nothing](mapSize = 1.byte).runRandomIO.right.value.runRandomIO.right.value
}

class MapSpec3 extends MapSpec {
  val keyValueCount: Int = 100000

  override def newDB(): Map[Int, String] =
    swaydb.extensions.memory.Map[Int, String, Nothing]().runRandomIO.right.value.runRandomIO.right.value
}

sealed trait MapSpec extends TestBaseEmbedded {

  val keyValueCount: Int

  def newDB(): Map[Int, String]

  implicit val mapKeySerializer = Key.serializer(IntSerializer)
  implicit val keyOrder: KeyOrder[Slice[Byte]] = KeyOrder.default

  "Extend" should {
    "initialise a rootMap" in {
      val rootMap = newDB()

      rootMap.stream.materialize.runRandomIO.right.value shouldBe empty

      //assert
      rootMap.baseMap().stream.materialize.runRandomIO.right.value shouldBe
        List(
          (Key.MapStart(Seq.empty), None),
          (Key.MapEntriesStart(Seq.empty), None),
          (Key.MapEntriesEnd(Seq.empty), None),
          (Key.SubMapsStart(Seq.empty), None),
          (Key.SubMapsEnd(Seq.empty), None),
          (Key.MapEnd(Seq.empty), None)
        )

      rootMap.closeDatabase().get
    }

    "update a rootMaps value" in {
      val rootMap = newDB()

      rootMap.getValue().runRandomIO.right.value shouldBe empty
      rootMap.updateValue("rootMap").runRandomIO.right.value
      rootMap.getValue().runRandomIO.right.value.value shouldBe "rootMap"

      //assert
      rootMap.baseMap().stream.materialize.runRandomIO.right.value shouldBe
        List(
          (Key.MapStart(Seq.empty), Some("rootMap")),
          (Key.MapEntriesStart(Seq.empty), None),
          (Key.MapEntriesEnd(Seq.empty), None),
          (Key.SubMapsStart(Seq.empty), None),
          (Key.SubMapsEnd(Seq.empty), None),
          (Key.MapEnd(Seq.empty), None)
        )

      rootMap.closeDatabase().get
    }

    "insert key-values to rootMap" in {
      val rootMap = newDB()
      rootMap.put(1, "one").runRandomIO.right.value
      rootMap.put(2, "two").runRandomIO.right.value

      rootMap.get(1).get.get shouldBe "one"
      rootMap.get(2).get.get shouldBe "two"

      rootMap.stream.materialize.runRandomIO.right.value shouldBe ListBuffer((1, "one"), (2, "two"))

      //assert
      rootMap.baseMap().stream.materialize.runRandomIO.right.value shouldBe
        ListBuffer(
          (Key.MapStart(Seq.empty), None),
          (Key.MapEntriesStart(Seq.empty), None),
          (Key.MapEntry(Seq.empty, 1), Some("one")),
          (Key.MapEntry(Seq.empty, 2), Some("two")),
          (Key.MapEntriesEnd(Seq.empty), None),
          (Key.SubMapsStart(Seq.empty), None),
          (Key.SubMapsEnd(Seq.empty), None),
          (Key.MapEnd(Seq.empty), None)
        )

      rootMap.closeDatabase().get
    }

    "insert a subMap" in {
      val rootMap = newDB()
      rootMap.put(1, "one").runRandomIO.right.value
      rootMap.put(2, "two").runRandomIO.right.value

      rootMap.maps.get(1).runRandomIO.right.value shouldBe empty

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value

      rootMap.maps.get(1).runRandomIO.right.value shouldBe defined

      subMap.put(1, "subMap one").runRandomIO.right.value
      subMap.put(2, "subMap two").runRandomIO.right.value

      rootMap.stream.materialize.runRandomIO.right.value shouldBe ListBuffer((1, "one"), (2, "two"))
      subMap.stream.materialize.runRandomIO.right.value shouldBe ListBuffer((1, "subMap one"), (2, "subMap two"))

      //assert
      rootMap.baseMap().stream.materialize.runRandomIO.right.value shouldBe
        List(
          (Key.MapStart(Seq.empty), None),
          (Key.MapEntriesStart(Seq.empty), None),
          (Key.MapEntry(Seq.empty, 1), Some("one")),
          (Key.MapEntry(Seq.empty, 2), Some("two")),
          (Key.MapEntriesEnd(Seq.empty), None),
          (Key.SubMapsStart(Seq.empty), None),
          (Key.SubMap(Seq.empty, 1), Some("sub map")),
          (Key.SubMapsEnd(Seq.empty), None),
          (Key.MapEnd(Seq.empty), None),

          //subMaps entries
          (Key.MapStart(Seq(1)), Some("sub map")),
          (Key.MapEntriesStart(Seq(1)), None),
          (Key.MapEntry(Seq(1), 1), Some("subMap one")),
          (Key.MapEntry(Seq(1), 2), Some("subMap two")),
          (Key.MapEntriesEnd(Seq(1)), None),
          (Key.SubMapsStart(Seq(1)), None),
          (Key.SubMapsEnd(Seq(1)), None),
          (Key.MapEnd(Seq(1)), None)
        )

      rootMap.closeDatabase().get
    }

    "remove all entries from rootMap and subMap" in {
      val rootMap = newDB()
      rootMap.put(1, "one").runRandomIO.right.value
      rootMap.put(2, "two").runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value

      subMap.put(1, "subMap one").runRandomIO.right.value
      subMap.put(2, "subMap two").runRandomIO.right.value

      eitherOne(
        left = {
          rootMap.clear().runRandomIO.right.value
          subMap.clear().runRandomIO.right.value
        },
        right = {
          rootMap.remove(1, 2).runRandomIO.right.value
          subMap.remove(1, 2).runRandomIO.right.value
        }
      )
      //assert
      //      rootMap.baseMap().toList shouldBe
      //        List(
      //          (Key.Start(Seq.empty), None),
      //          (Key.EntriesStart(Seq.empty), None),
      //          (Key.EntriesEnd(Seq.empty), None),
      //          (Key.SubMapsStart(Seq.empty), None),
      //          (Key.SubMap(Seq.empty, 1), Some("sub map")),
      //          (Key.SubMapsEnd(Seq.empty), None),
      //          (Key.End(Seq.empty), None),
      //
      //          //subMaps entries
      //          (Key.Start(Seq(1)), Some("sub map")),
      //          (Key.EntriesStart(Seq(1)), None),
      //          (Key.EntriesEnd(Seq(1)), None),
      //          (Key.SubMapsStart(Seq(1)), None),
      //          (Key.SubMapsEnd(Seq(1)), None),
      //          (Key.End(Seq(1)), None)
      //        )

      rootMap.closeDatabase().get
    }

    "update a subMap's value" in {
      val rootMap = newDB()

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      rootMap.maps.updateValue(1, "sub map updated")
      rootMap.maps.contains(1).runRandomIO.right.value shouldBe true

      //assert
      //      rootMap.baseMap().toList shouldBe
      //        List(
      //          (Key.Start(Seq.empty), None),
      //          (Key.EntriesStart(Seq.empty), None),
      //          (Key.EntriesEnd(Seq.empty), None),
      //          (Key.SubMapsStart(Seq.empty), None),
      //          (Key.SubMap(Seq.empty, 1), Some("sub map updated")),
      //          (Key.SubMapsEnd(Seq.empty), None),
      //          (Key.End(Seq.empty), None),
      //
      //          //subMaps entries
      //          (Key.Start(Seq(1)), Some("sub map updated")),
      //          (Key.EntriesStart(Seq(1)), None),
      //          (Key.EntriesEnd(Seq(1)), None),
      //          (Key.SubMapsStart(Seq(1)), None),
      //          (Key.SubMapsEnd(Seq(1)), None),
      //          (Key.End(Seq(1)), None)
      //        )

      rootMap.closeDatabase().get
    }

    "getMap, containsMap, exists & getMapValue" in {
      val rootMap = newDB()

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      subMap.put(1, "one").runRandomIO.right.value
      subMap.put(2, "two").runRandomIO.right.value

      val subMapGet = rootMap.maps.get(1).runRandomIO.right.value.value
      subMapGet.getValue().runRandomIO.right.value.value shouldBe "sub map"
      subMapGet.stream.materialize.runRandomIO.right.value shouldBe ListBuffer((1, "one"), (2, "two"))

      rootMap.maps.contains(1).runRandomIO.right.value shouldBe true
      rootMap.exists().runRandomIO.right.value shouldBe true
      subMap.exists().runRandomIO.right.value shouldBe true
      rootMap.maps.getValue(1).runRandomIO.right.value.value shouldBe "sub map"
      rootMap.maps.getValue(2).runRandomIO.right.value shouldBe empty //2 does not exists

      rootMap.maps.remove(1).runRandomIO.right.value

      rootMap.maps.contains(1).runRandomIO.right.value shouldBe false
      rootMap.exists().runRandomIO.right.value shouldBe true
      subMap.exists().runRandomIO.right.value shouldBe false
      rootMap.maps.getValue(1).runRandomIO.right.value shouldBe empty //is deleted

      rootMap.closeDatabase().get
    }

    "expire key" in {
      val rootMap = newDB()
      rootMap.put(1, "one", 500.millisecond).runRandomIO.right.value
      rootMap.put(2, "two").runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value

      subMap.put(1, "subMap one", 500.millisecond).runRandomIO.right.value
      subMap.put(2, "subMap two").runRandomIO.right.value

      eventual {
        rootMap.get(1).runRandomIO.right.value shouldBe empty
        subMap.get(1).runRandomIO.right.value shouldBe empty
      }

      //assert
      //      rootMap.baseMap().toList shouldBe
      //        List(
      //          (Key.Start(Seq.empty), None),
      //          (Key.EntriesStart(Seq.empty), None),
      //          //          (Key.Entry(Seq.empty, 1), Some("one")),//expired
      //          (Key.Entry(Seq.empty, 2), Some("two")),
      //          (Key.EntriesEnd(Seq.empty), None),
      //          (Key.SubMapsStart(Seq.empty), None),
      //          (Key.SubMap(Seq.empty, 1), Some("sub map")),
      //          (Key.SubMapsEnd(Seq.empty), None),
      //          (Key.End(Seq.empty), None),
      //
      //          //subMaps entries
      //          (Key.Start(Seq(1)), Some("sub map")),
      //          (Key.EntriesStart(Seq(1)), None),
      //          //          (Key.Entry(Seq(1), 1), Some("subMap one")), //expired
      //          (Key.Entry(Seq(1), 2), Some("subMap two")),
      //          (Key.EntriesEnd(Seq(1)), None),
      //          (Key.SubMapsStart(Seq(1)), None),
      //          (Key.SubMapsEnd(Seq(1)), None),
      //          (Key.End(Seq(1)), None)
      //        )

      rootMap.closeDatabase().get
    }

    "expire range keys" in {
      val rootMap = newDB()
      rootMap.put(1, "one").runRandomIO.right.value
      rootMap.put(2, "two").runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value

      subMap.put(1, "subMap two").runRandomIO.right.value
      subMap.put(2, "subMap two").runRandomIO.right.value
      subMap.put(3, "subMap two").runRandomIO.right.value
      subMap.put(4, "subMap two").runRandomIO.right.value

      rootMap.expire(1, 2, 100.millisecond).runRandomIO.right.value //expire all key-values from rootMap
      subMap.expire(2, 3, 100.millisecond).runRandomIO.right.value //expire some from subMap

      eventual {
        rootMap.get(1).runRandomIO.right.value shouldBe empty
        rootMap.get(2).runRandomIO.right.value shouldBe empty
        subMap.get(1).runRandomIO.right.value.value shouldBe "subMap two"
        subMap.get(2).runRandomIO.right.value shouldBe empty
        subMap.get(3).runRandomIO.right.value shouldBe empty
        subMap.get(4).runRandomIO.right.value.value shouldBe "subMap two"
      }

      //assert
      //      rootMap.baseMap().toList shouldBe
      //        List(
      //          (Key.Start(Seq.empty), None),
      //          (Key.EntriesStart(Seq.empty), None),
      //          (Key.EntriesEnd(Seq.empty), None),
      //          (Key.SubMapsStart(Seq.empty), None),
      //          (Key.SubMap(Seq.empty, 1), Some("sub map")),
      //          (Key.SubMapsEnd(Seq.empty), None),
      //          (Key.End(Seq.empty), None),
      //
      //          //subMaps entries
      //          (Key.Start(Seq(1)), Some("sub map")),
      //          (Key.EntriesStart(Seq(1)), None),
      //          (Key.Entry(Seq(1), 1), Some("subMap two")),
      //          (Key.Entry(Seq(1), 4), Some("subMap two")),
      //          (Key.EntriesEnd(Seq(1)), None),
      //          (Key.SubMapsStart(Seq(1)), None),
      //          (Key.SubMapsEnd(Seq(1)), None),
      //          (Key.End(Seq(1)), None)
      //        )

      rootMap.closeDatabase().get
    }

    "update range keys" in {
      val rootMap = newDB()
      rootMap.put(1, "one").runRandomIO.right.value
      rootMap.put(2, "two").runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value

      subMap.put(1, "subMap two").runRandomIO.right.value
      subMap.put(2, "subMap two").runRandomIO.right.value
      subMap.put(3, "subMap two").runRandomIO.right.value
      subMap.put(4, "subMap two").runRandomIO.right.value

      eitherOne(
        left = {
          rootMap.update(1, 2, "updated").runRandomIO.right.value //update all key-values from rootMap
          subMap.update(2, 3, "updated").runRandomIO.right.value //update some from subMap
        },
        right = {
          rootMap.update(1, "updated").runRandomIO.right.value
          rootMap.update(2, "updated").runRandomIO.right.value
          subMap.update(2, "updated").runRandomIO.right.value
          subMap.update(3, "updated").runRandomIO.right.value
        }
      )

      rootMap.get(1).runRandomIO.right.value.value shouldBe "updated"
      rootMap.get(2).runRandomIO.right.value.value shouldBe "updated"
      subMap.get(2).runRandomIO.right.value.value shouldBe "updated"
      subMap.get(3).runRandomIO.right.value.value shouldBe "updated"

      rootMap.closeDatabase().get
    }

    "batch put" in {
      val rootMap = newDB()
      rootMap.commitPrepared(
        Prepare.Put(1, "one"),
        Prepare.Put(2, "two")
      ).runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      subMap.commitPrepared(
        Prepare.Put(1, "one one"),
        Prepare.Put(2, "two two")
      ).runRandomIO.right.value

      rootMap.get(1).runRandomIO.right.value.value shouldBe "one"
      rootMap.get(2).runRandomIO.right.value.value shouldBe "two"
      subMap.get(1).runRandomIO.right.value.value shouldBe "one one"
      subMap.get(2).runRandomIO.right.value.value shouldBe "two two"

      rootMap.closeDatabase().get
    }

    "batch update" in {
      val rootMap = newDB()
      rootMap.commitPrepared(
        Prepare.Put(1, "one"),
        Prepare.Put(2, "two")
      ).runRandomIO.right.value

      rootMap.commitPrepared(
        Prepare.Update(1, "one updated"),
        Prepare.Update(2, "two updated")
      ).runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      subMap.commitPrepared(
        Prepare.Put(1, "one one"),
        Prepare.Put(2, "two two")
      ).runRandomIO.right.value

      subMap.commitPrepared(
        Prepare.Update(1, "one one updated"),
        Prepare.Update(2, "two two updated")
      ).runRandomIO.right.value

      rootMap.get(1).runRandomIO.right.value.value shouldBe "one updated"
      rootMap.get(2).runRandomIO.right.value.value shouldBe "two updated"
      subMap.get(1).runRandomIO.right.value.value shouldBe "one one updated"
      subMap.get(2).runRandomIO.right.value.value shouldBe "two two updated"

      rootMap.closeDatabase().get
    }

    "batch expire" in {
      val rootMap = newDB()
      rootMap.commitPrepared(
        Prepare.Put(1, "one"),
        Prepare.Put(2, "two")
      ).runRandomIO.right.value

      rootMap.commitPrepared(
        Prepare.Expire(1, 100.millisecond),
        Prepare.Expire(2, 100.millisecond)
      ).runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      subMap.commitPrepared(
        Prepare.Put(1, "one one"),
        Prepare.Put(2, "two two")
      ).runRandomIO.right.value

      subMap.commitPrepared(
        Prepare.Expire(1, 100.millisecond),
        Prepare.Expire(2, 100.millisecond)
      ).runRandomIO.right.value

      eventual {
        rootMap.stream.materialize.runRandomIO.right.value shouldBe empty
        subMap.stream.materialize.runRandomIO.right.value shouldBe empty
      }

      rootMap.closeDatabase().get
    }

    "batchPut" in {
      val rootMap = newDB()
      rootMap.put((1, "one"), (2, "two")).runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      subMap.put((1, "one one"), (2, "two two"))

      rootMap.stream.materialize.runRandomIO.right.value shouldBe ListBuffer((1, "one"), (2, "two"))
      subMap.stream.materialize.runRandomIO.right.value shouldBe ListBuffer((1, "one one"), (2, "two two"))

      rootMap.closeDatabase().get
    }

    "batchUpdate" in {
      val rootMap = newDB()
      rootMap.put((1, "one"), (2, "two")).runRandomIO.right.value
      rootMap.update((1, "one updated"), (2, "two updated")).runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      subMap.put((1, "one one"), (2, "two two"))
      subMap.update((1, "one one updated"), (2, "two two updated")).runRandomIO.right.value

      rootMap.stream.materialize.runRandomIO.right.value shouldBe ListBuffer((1, "one updated"), (2, "two updated"))
      subMap.stream.materialize.runRandomIO.right.value shouldBe ListBuffer((1, "one one updated"), (2, "two two updated"))

      rootMap.closeDatabase().get
    }

    "batchRemove" in {
      val rootMap = newDB()
      rootMap.put((1, "one"), (2, "two")).runRandomIO.right.value
      rootMap.remove(1, 2).runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      subMap.put((1, "one one"), (2, "two two"))
      subMap.remove(1, 2).runRandomIO.right.value

      rootMap.stream.materialize.runRandomIO.right.value shouldBe empty
      subMap.stream.materialize.runRandomIO.right.value shouldBe empty

      rootMap.closeDatabase().get
    }

    "batchExpire" in {
      val rootMap = newDB()
      rootMap.put((1, "one"), (2, "two")).runRandomIO.right.value
      rootMap.expire((1, 1.second.fromNow)).runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      subMap.put((1, "one one"), (2, "two two"))
      subMap.expire((1, 1.second.fromNow), (2, 1.second.fromNow)).runRandomIO.right.value

      eventual {
        rootMap.stream.materialize.runRandomIO.right.value should contain only ((2, "two"))
        subMap.stream.materialize.runRandomIO.right.value shouldBe empty
      }

      rootMap.closeDatabase().get
    }

    "get" in {
      val rootMap = newDB()
      rootMap.put((1, "one"), (2, "two")).runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      subMap.put((1, "one one"), (2, "two two"))

      rootMap.get(1).runRandomIO.right.value.value shouldBe "one"
      rootMap.get(2).runRandomIO.right.value.value shouldBe "two"
      subMap.get(1).runRandomIO.right.value.value shouldBe "one one"
      subMap.get(2).runRandomIO.right.value.value shouldBe "two two"

      rootMap.remove(1, 2).runRandomIO.right.value
      subMap.remove(1, 2).runRandomIO.right.value

      rootMap.get(1).runRandomIO.right.value shouldBe empty
      rootMap.get(2).runRandomIO.right.value shouldBe empty
      subMap.get(1).runRandomIO.right.value shouldBe empty
      subMap.get(2).runRandomIO.right.value shouldBe empty

      rootMap.closeDatabase().get
    }

    "value when sub map is removed" in {
      val rootMap = newDB()
      rootMap.put((1, "one"), (2, "two")).runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      subMap.put((1, "one one"), (2, "two two"))

      rootMap.get(1).runRandomIO.right.value.value shouldBe "one"
      rootMap.get(2).runRandomIO.right.value.value shouldBe "two"
      subMap.get(1).runRandomIO.right.value.value shouldBe "one one"
      subMap.get(2).runRandomIO.right.value.value shouldBe "two two"

      rootMap.remove(1, 2).runRandomIO.right.value
      rootMap.maps.remove(1).runRandomIO.right.value

      rootMap.get(1).runRandomIO.right.value shouldBe empty
      rootMap.get(2).runRandomIO.right.value shouldBe empty
      subMap.get(1).runRandomIO.right.value shouldBe empty
      subMap.get(2).runRandomIO.right.value shouldBe empty

      rootMap.closeDatabase().get
    }

    "getKey" in {
      val rootMap = newDB()
      rootMap.put((1, "one"), (2, "two")).runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      subMap.put((11, "one one"), (22, "two two"))

      rootMap.getKey(1).runRandomIO.right.value.value shouldBe 1
      rootMap.getKey(2).runRandomIO.right.value.value shouldBe 2
      subMap.getKey(11).runRandomIO.right.value.value shouldBe 11
      subMap.getKey(22).runRandomIO.right.value.value shouldBe 22

      rootMap.remove(1, 2).runRandomIO.right.value
      rootMap.maps.remove(1).runRandomIO.right.value

      rootMap.get(1).runRandomIO.right.value shouldBe empty
      rootMap.get(2).runRandomIO.right.value shouldBe empty
      subMap.get(11).runRandomIO.right.value shouldBe empty
      subMap.get(22).runRandomIO.right.value shouldBe empty

      rootMap.closeDatabase().get
    }

    "getKeyValue" in {
      val rootMap = newDB()
      rootMap.put((1, "one"), (2, "two")).runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      subMap.put((11, "one one"), (22, "two two"))

      rootMap.getKeyValue(1).runRandomIO.right.value.value shouldBe(1, "one")
      rootMap.getKeyValue(2).runRandomIO.right.value.value shouldBe(2, "two")
      subMap.getKeyValue(11).runRandomIO.right.value.value shouldBe(11, "one one")
      subMap.getKeyValue(22).runRandomIO.right.value.value shouldBe(22, "two two")

      rootMap.remove(1, 2).runRandomIO.right.value
      rootMap.maps.remove(1).runRandomIO.right.value

      rootMap.getKeyValue(1).runRandomIO.right.value shouldBe empty
      rootMap.getKeyValue(2).runRandomIO.right.value shouldBe empty
      subMap.getKeyValue(11).runRandomIO.right.value shouldBe empty
      subMap.getKeyValue(22).runRandomIO.right.value shouldBe empty

      rootMap.closeDatabase().get
    }

    "keys" in {
      val rootMap = newDB()
      rootMap.put((1, "one"), (2, "two")).runRandomIO.right.value

      val subMap = rootMap.maps.put(1, "sub map").runRandomIO.right.value
      subMap.put((11, "one one"), (22, "two two"))

      rootMap.keys.stream.materialize.runRandomIO.right.value should contain inOrderOnly(1, 2)
      subMap.keys.stream.materialize.runRandomIO.right.value should contain inOrderOnly(11, 22)

      rootMap.closeDatabase().get
    }
  }

  "Map" should {

    "return entries ranges" in {
      Map.entriesRangeKeys(Seq(1, 2, 3)) shouldBe ((Key.MapEntriesStart(Seq(1, 2, 3)), Key.MapEntriesEnd(Seq(1, 2, 3))))
    }

    "return empty subMap range keys for a empty SubMap" in {
      val db = newDB()

      val rootMap = db.maps.put(1, "rootMap").runRandomIO.right.value
      Map.childSubMapRanges(rootMap).get shouldBe empty

      db.closeDatabase().get
    }

    "return subMap that has only one child subMap" in {
      val rootMap = newDB()

      val firstMap = rootMap.maps.put(1, "rootMap").runRandomIO.right.value
      val secondMap = firstMap.maps.put(2, "second map").runRandomIO.right.value

      Map.childSubMapRanges(firstMap).get should contain only ((Key.SubMap(Seq(1), 2), Key.MapStart(Seq(1, 2)), Key.MapEnd(Seq(1, 2))))
      Map.childSubMapRanges(secondMap).get shouldBe empty

      rootMap.closeDatabase().get
    }

    "return subMaps of 3 nested maps" in {
      val db = newDB()

      val firstMap = db.maps.put(1, "first").runRandomIO.right.value
      val secondMap = firstMap.maps.put(2, "second").runRandomIO.right.value
      val thirdMap = secondMap.maps.put(2, "third").runRandomIO.right.value

      Map.childSubMapRanges(firstMap).get should contain inOrderOnly((Key.SubMap(Seq(1), 2), Key.MapStart(Seq(1, 2)), Key.MapEnd(Seq(1, 2))), (Key.SubMap(Seq(1, 2), 2), Key.MapStart(Seq(1, 2, 2)), Key.MapEnd(Seq(1, 2, 2))))
      Map.childSubMapRanges(secondMap).get should contain only ((Key.SubMap(Seq(1, 2), 2), Key.MapStart(Seq(1, 2, 2)), Key.MapEnd(Seq(1, 2, 2))))
      Map.childSubMapRanges(thirdMap).get shouldBe empty

      db.closeDatabase().get
    }

    "returns multiple child subMap that also contains nested subMaps" in {
      val db = newDB()

      val firstMap = db.maps.put(1, "firstMap").runRandomIO.right.value
      val secondMap = firstMap.maps.put(2, "subMap").runRandomIO.right.value

      secondMap.maps.put(2, "subMap").runRandomIO.right.value
      secondMap.maps.put(3, "subMap3").runRandomIO.right.value
      val subMap4 = secondMap.maps.put(4, "subMap4").runRandomIO.right.value
      subMap4.maps.put(44, "subMap44").runRandomIO.right.value
      val subMap5 = secondMap.maps.put(5, "subMap5").runRandomIO.right.value
      val subMap55 = subMap5.maps.put(55, "subMap55").runRandomIO.right.value
      subMap55.maps.put(5555, "subMap55").runRandomIO.right.value
      subMap55.maps.put(6666, "subMap55").runRandomIO.right.value
      subMap5.maps.put(555, "subMap555").runRandomIO.right.value

      val mapHierarchy =
        List(
          (Key.SubMap(Seq(1), 2), Key.MapStart(Seq(1, 2)), Key.MapEnd(Seq(1, 2))),
          (Key.SubMap(Seq(1, 2), 2), Key.MapStart(Seq(1, 2, 2)), Key.MapEnd(Seq(1, 2, 2))),
          (Key.SubMap(Seq(1, 2), 3), Key.MapStart(Seq(1, 2, 3)), Key.MapEnd(Seq(1, 2, 3))),
          (Key.SubMap(Seq(1, 2), 4), Key.MapStart(Seq(1, 2, 4)), Key.MapEnd(Seq(1, 2, 4))),
          (Key.SubMap(Seq(1, 2, 4), 44), Key.MapStart(Seq(1, 2, 4, 44)), Key.MapEnd(Seq(1, 2, 4, 44))),
          (Key.SubMap(Seq(1, 2), 5), Key.MapStart(Seq(1, 2, 5)), Key.MapEnd(Seq(1, 2, 5))),
          (Key.SubMap(Seq(1, 2, 5), 55), Key.MapStart(Seq(1, 2, 5, 55)), Key.MapEnd(Seq(1, 2, 5, 55))),
          (Key.SubMap(Seq(1, 2, 5, 55), 5555), Key.MapStart(Seq(1, 2, 5, 55, 5555)), Key.MapEnd(Seq(1, 2, 5, 55, 5555))),
          (Key.SubMap(Seq(1, 2, 5, 55), 6666), Key.MapStart(Seq(1, 2, 5, 55, 6666)), Key.MapEnd(Seq(1, 2, 5, 55, 6666))),
          (Key.SubMap(Seq(1, 2, 5), 555), Key.MapStart(Seq(1, 2, 5, 555)), Key.MapEnd(Seq(1, 2, 5, 555)))
        )

      Map.childSubMapRanges(firstMap).get shouldBe mapHierarchy
      Map.childSubMapRanges(secondMap).get shouldBe mapHierarchy.drop(1)

      db.closeDatabase().get
    }
  }

  "SubMap" when {
    "maps.put on a non existing map" should {
      "create a new subMap" in {
        val root = newDB()

        val first = root.maps.put(1, "first").runRandomIO.right.value
        val second = first.maps.put(2, "second").runRandomIO.right.value
        first.maps.get(2).runRandomIO.right.value shouldBe defined
        second.maps.get(2).runRandomIO.right.value shouldBe empty

        root.closeDatabase().get
      }
    }

    "maps.put on a existing map" should {
      "replace existing map" in {
        val root = newDB()

        val first = root.maps.put(1, "first").runRandomIO.right.value
        val second = first.maps.put(2, "second").runRandomIO.right.value
        val secondAgain = first.maps.put(2, "second again").runRandomIO.right.value

        first.maps.get(2).runRandomIO.right.value shouldBe defined
        first.maps.getValue(2).runRandomIO.right.value.value shouldBe "second again"
        second.getValue().runRandomIO.right.value.value shouldBe "second again"
        secondAgain.getValue().runRandomIO.right.value.value shouldBe "second again"

        root.closeDatabase().get
      }

      "replace existing map and all it's entries" in {
        val root = newDB()

        val first = root.maps.put(1, "first").runRandomIO.right.value
        val second = first.maps.put(2, "second").runRandomIO.right.value
        //write entries to second map
        second.put(1, "one").runRandomIO.right.value
        second.put(2, "two").runRandomIO.right.value
        second.put(3, "three").runRandomIO.right.value
        //assert second map has these entries
        second.stream.materialize.runRandomIO.right.value shouldBe List((1, "one"), (2, "two"), (3, "three"))

        val secondAgain = first.maps.put(2, "second again").runRandomIO.right.value

        //map value value updated
        first.maps.get(2).runRandomIO.right.value shouldBe defined
        first.maps.getValue(2).runRandomIO.right.value.value shouldBe "second again"
        second.getValue().runRandomIO.right.value.value shouldBe "second again"
        secondAgain.getValue().runRandomIO.right.value.value shouldBe "second again"
        //all the old entries are removed
        second.stream.materialize.runRandomIO.right.value shouldBe empty

        root.closeDatabase().get
      }

      "replace existing map and all it's entries and also all existing maps subMap and all their entries" in {
        val root = newDB()

        //MAP HIERARCHY
        //first
        //   second
        //       third
        //           fourth
        val first = root.maps.put(1, "first").runRandomIO.right.value
        val second = first.maps.put(2, "second").runRandomIO.right.value
        second.put(1, "second one").runRandomIO.right.value
        second.put(2, "second two").runRandomIO.right.value
        second.put(3, "second three").runRandomIO.right.value
        //third map that is the child map of second map
        val third = second.maps.put(3, "third").runRandomIO.right.value
        third.put(1, "third one").runRandomIO.right.value
        third.put(2, "third two").runRandomIO.right.value
        third.put(3, "third three").runRandomIO.right.value
        val fourth = third.maps.put(4, "fourth").runRandomIO.right.value
        fourth.put(1, "fourth one").runRandomIO.right.value
        fourth.put(2, "fourth two").runRandomIO.right.value
        fourth.put(3, "fourth three").runRandomIO.right.value

        /**
         * Assert that the all maps' content is accurate
         */
        second.stream.materialize.runRandomIO.right.value shouldBe List((1, "second one"), (2, "second two"), (3, "second three"))
        third.stream.materialize.runRandomIO.right.value shouldBe List((1, "third one"), (2, "third two"), (3, "third three"))
        fourth.stream.materialize.runRandomIO.right.value shouldBe List((1, "fourth one"), (2, "fourth two"), (3, "fourth three"))

        second.stream.materialize.runRandomIO.right.value shouldBe List((1, "second one"), (2, "second two"), (3, "second three"))
        third.stream.materialize.runRandomIO.right.value shouldBe List((1, "third one"), (2, "third two"), (3, "third three"))
        fourth.stream.materialize.runRandomIO.right.value shouldBe List((1, "fourth one"), (2, "fourth two"), (3, "fourth three"))

        second.maps.stream.materialize.runRandomIO.right.value shouldBe List((3, "third"))
        third.maps.stream.materialize.runRandomIO.right.value shouldBe List((4, "fourth"))
        fourth.maps.stream.materialize.runRandomIO.right.value shouldBe empty

        //submit put on second map and assert that all it's contents are replaced.
        val secondAgain = first.maps.put(2, "second updated").runRandomIO.right.value

        //map value value updated
        first.maps.get(2).runRandomIO.right.value shouldBe defined
        first.maps.getValue(2).runRandomIO.right.value.value shouldBe "second updated"
        second.getValue().runRandomIO.right.value.value shouldBe "second updated"
        secondAgain.getValue().runRandomIO.right.value.value shouldBe "second updated"
        //all the old entries are removed
        second.stream.materialize.runRandomIO.right.value shouldBe empty
        third.stream.materialize.runRandomIO.right.value shouldBe empty
        fourth.stream.materialize.runRandomIO.right.value shouldBe empty

        second.maps.contains(3).runRandomIO.right.value shouldBe false
        second.maps.contains(4).runRandomIO.right.value shouldBe false

        root.closeDatabase().get
      }
    }

    "clear" should {
      "remove all key-values from a map" in {

        val root = newDB()
        val first = root.maps.put(1, "first").runRandomIO.right.value
        val second = first.maps.put(2, "second").runRandomIO.right.value
        second.put(1, "second one").runRandomIO.right.value
        second.put(2, "second two").runRandomIO.right.value
        second.put(3, "second three").runRandomIO.right.value
        //third map that is the child map of second map
        val third = second.maps.put(3, "third").runRandomIO.right.value
        third.put(1, "third one").runRandomIO.right.value
        third.put(2, "third two").runRandomIO.right.value
        third.put(3, "third three").runRandomIO.right.value

        second.stream.materialize.runRandomIO.right.value should have size 3
        second.clear().runRandomIO.right.value
        second.stream.materialize.runRandomIO.right.value shouldBe empty

        third.stream.materialize.runRandomIO.right.value should have size 3
        second.maps.clear(3).runRandomIO.right.value
        third.stream.materialize.runRandomIO.right.value shouldBe empty

        root.closeDatabase().get
      }
    }
  }
}
