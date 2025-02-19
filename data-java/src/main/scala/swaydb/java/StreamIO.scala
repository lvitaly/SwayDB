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

package swaydb.java

import java.util.Optional
import java.util.function.{BiFunction, Consumer, Predicate}

import swaydb.java.data.util.Java._

import scala.jdk.CollectionConverters._
import scala.compat.java8.FunctionConverters._

class StreamIO[A](val asScala: swaydb.Stream[A, swaydb.IO.ThrowableIO]) {
  implicit val javaThrowableExceptionHandler = swaydb.java.IO.throwableExceptionHandler

  def forEach(consumer: Consumer[A]): StreamIO[Unit] =
    new StreamIO[Unit](asScala.foreach(consumer.asScala))

  def map[B](function: JavaFunction[A, B]): StreamIO[B] =
    Stream.fromScala(asScala.map(function.asScala))

  def flatMap[B](function: JavaFunction[A, StreamIO[B]]): StreamIO[B] =
    Stream.fromScala(asScala.flatMap(function.asScala(_).asScala))

  def drop(count: Int): StreamIO[A] =
    Stream.fromScala(asScala.drop(count))

  def dropWhile(predicate: Predicate[A]): StreamIO[A] =
    Stream.fromScala(asScala.dropWhile(predicate.test))

  def take(count: Int): StreamIO[A] =
    Stream.fromScala(asScala.take(count))

  def takeWhile(predicate: Predicate[A]): StreamIO[A] =
    Stream.fromScala(asScala.takeWhile(predicate.test))

  def filter(predicate: Predicate[A]): StreamIO[A] =
    Stream.fromScala(asScala.filter(predicate.test))

  def filterNot(predicate: Predicate[A]): StreamIO[A] =
    Stream.fromScala(asScala.filterNot(predicate.test))

  def lastOption: IO[Throwable, Optional[A]] =
    new IO(asScala.lastOption.map(_.asJava))

  def headOption: IO[Throwable, Optional[A]] =
    new IO(asScala.headOption.map(_.asJava))

  def foldLeft[B](initial: B, function: BiFunction[B, A, B]): IO[Throwable, B] =
    new IO(asScala.foldLeft(initial)(function.asScala))

  def count(predicate: Predicate[A]): IO[Throwable, Int] =
    new IO(asScala.count(predicate.test))

  def size: IO[Throwable, Int] =
    new IO(asScala.size)

  def materialize: IO[Throwable, java.util.List[A]] =
    new IO(asScala.materialize.map(_.asJava))
}
