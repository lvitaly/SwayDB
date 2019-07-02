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

package swaydb.core.util.cache

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import swaydb.core.RunThis._
import swaydb.data.IO

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

class CacheSpec extends WordSpec with Matchers with MockFactory {

  "Cache.io" should {
    "invoke the init function only once on success" in {
      val mock = mockFunction[IO[Int]]

      val cache = Cache.io[Int](synchronised = Random.nextBoolean(), stored = true)(mock.apply())
      cache.isCached shouldBe false
      mock.expects() returning IO(123)

      cache.value shouldBe IO.Success(123)
      cache.isCached shouldBe true
      cache.value shouldBe IO.Success(123) //value again mock function is not invoked again
    }

    "not cache on failure" in {
      val mock = mockFunction[IO[Int]]

      val cache = Cache.io[Int](synchronised = Random.nextBoolean(), stored = true)(mock.apply())
      cache.isCached shouldBe false
      mock.expects() returning IO.Failure("Kaboom!")

      //failure
      cache.value.failed.get.exception.getMessage shouldBe "Kaboom!"
      cache.isCached shouldBe false

      //success
      mock.expects() returning IO(123)
      cache.value shouldBe IO.Success(123) //value again mock function is not invoked again
      cache.isCached shouldBe true
    }

    "concurrent access to reserved io" should {
      "not be allowed" in {
        val cache =
          Cache.io(synchronised = false, stored = true) {
            sleep(200.millisecond) //delay access
            IO.Success(10)
          }

        val futures =
        //concurrently do requests
          Future.sequence {
            (1 to 100) map {
              _ =>
                Future().flatMap(_ => cache.value.toFuture)
            }
          }

        //results in failure since some thread has reserved.
        val failure = futures.failed.await
        failure shouldBe a[IO.Exception.ReservedValue]

        //eventually it's freed
        eventual {
          failure.asInstanceOf[IO.Exception.ReservedValue].busy.isBusy shouldBe false
        }
      }
    }
  }
}
