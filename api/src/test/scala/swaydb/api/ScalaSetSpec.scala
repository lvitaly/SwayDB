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

package swaydb.api

import swaydb._
import swaydb.core.IOAssert._
import swaydb.core.RunThis._
import swaydb.data.IO
import swaydb.serializers.Default._

class ScalaSetSpec0 extends ScalaSetSpec {
  val keyValueCount: Int = 1000

  override def newDB(): Set[Int, IO] =
    swaydb.persistent.Set[Int](dir = randomDir).assertGet
}

class ScalaSetSpec1 extends ScalaSetSpec {

  val keyValueCount: Int = 1000

  override def newDB(): Set[Int, IO] =
    swaydb.persistent.Set[Int](randomDir, mapSize = 1.byte, segmentSize = 10.bytes).assertGet
}

class ScalaSetSpec2 extends ScalaSetSpec {

  val keyValueCount: Int = 10000

  override def newDB(): Set[Int, IO] =
    swaydb.memory.Set[Int](mapSize = 1.byte).assertGet
}

class ScalaSetSpec3 extends ScalaSetSpec {
  val keyValueCount: Int = 10000

  override def newDB(): Set[Int, IO] =
    swaydb.memory.Set[Int]().assertGet
}

class ScalaSetSpec4 extends ScalaSetSpec {

  val keyValueCount: Int = 10000

  override def newDB(): Set[Int, IO] =
    swaydb.memory.zero.Set[Int](mapSize = 1.byte).assertGet
}

class ScalaSetSpec5 extends ScalaSetSpec {
  val keyValueCount: Int = 10000

  override def newDB(): Set[Int, IO] =
    swaydb.memory.zero.Set[Int]().assertGet
}

sealed trait ScalaSetSpec extends TestBaseEmbedded {

  val keyValueCount: Int

  def newDB(): Set[Int, IO]

  "Expire" when {
    "put" in {
      val db = newDB()

      db.asScala.add(1)

      db.asScala.contains(1) shouldBe true
    }

    "putAll" in {
      val db = newDB()

      db.asScala ++= Seq(1, 2)

      db.asScala.contains(1) shouldBe true
      db.asScala.contains(2) shouldBe true
    }

    "remove" in {
      val db = newDB()

      db.asScala ++= Seq(1, 2)

      db.asScala.remove(1)

      db.asScala.contains(1) shouldBe false
      db.asScala.contains(2) shouldBe true
    }

    "removeAll" in {
      val db = newDB()

      db.asScala ++= Seq(1, 2)

      db.asScala.clear()

      db.asScala.contains(1) shouldBe false
      db.asScala.contains(2) shouldBe false
    }

    "head, last, contains" in {
      val db = newDB()

      db.asScala ++= Seq(1, 2)

      db.asScala.head shouldBe 1
      db.asScala.last shouldBe 2

      db.asScala.contains(1) shouldBe true
      db.asScala.contains(2) shouldBe true
      db.asScala.contains(3) shouldBe false

      db.closeDatabase().get

    }
  }
}