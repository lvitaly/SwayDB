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
package swaydb.core.actor

import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import swaydb.data.config.{ActorConfig, FileCache}
import swaydb.{IO, Tagged}

import scala.concurrent.ExecutionContext
import scala.ref.WeakReference


private[core] trait FileSweeperItem {
  def path: Path
  def delete(): IO[swaydb.Error.Segment, Unit]
  def close(): IO[swaydb.Error.Segment, Unit]
  def isOpen: Boolean
}

private[swaydb] trait FileSweeper extends Tagged[FileSweeper.Enabled, Option]
private[swaydb] object FileSweeper extends LazyLogging {

  case object Disabled extends FileSweeper {
    override def get: Option[Enabled] = None
  }
  sealed trait Enabled extends FileSweeper {
    override def get: Option[Enabled] = Some(this)
    def ec: ExecutionContext
    def close(file: FileSweeperItem): Unit
    def delete(file: FileSweeperItem): Unit
    def terminate(): Unit
  }

  private sealed trait Action {
    def isDelete: Boolean
  }
  private object Action {
    case class Delete(file: FileSweeperItem) extends Action {
      def isDelete: Boolean = true
    }
    case class Close(file: WeakReference[FileSweeperItem]) extends Action {
      def isDelete: Boolean = false
    }
  }

  def weigher(action: Action) =
    if (action.isDelete) 10 else 1

  def apply(fileCache: FileCache): Option[FileSweeper.Enabled] =
    fileCache map apply

  def apply(fileCache: FileCache.Enable): FileSweeper.Enabled =
    apply(
      maxOpenSegments = fileCache.maxOpen,
      actorQueue = fileCache.actorQueue
    )

  def apply(maxOpenSegments: Long, actorQueue: ActorConfig): FileSweeper.Enabled = {
    lazy val queue = CacheActor[Action](maxOpenSegments, actorQueue, weigher) {
      case Action.Delete(file) =>
        file.delete() onFailureSideEffect {
          error =>
            logger.error(s"Failed to delete file. ${file.path}", error.exception)
        }

      case Action.Close(file) =>
        file.get foreach {
          file =>
            file.close onFailureSideEffect {
              error =>
                logger.error(s"Failed to close file. ${file.path}", error.exception)
            }
        }
    }

    new FileSweeper.Enabled {

      def ec = actorQueue.ec

      override def close(file: FileSweeperItem): Unit =
        queue ! Action.Close(new WeakReference[FileSweeperItem](file))

      //Delete cannot be a WeakReference because Levels can
      //remove references to the file after eventualDelete is invoked.
      //If the file gets garbage collected due to it being WeakReference before
      //delete on the file is triggered, the physical file will remain on disk.
      override def delete(file: FileSweeperItem): Unit =
        queue ! Action.Delete(file)

      override def terminate(): Unit =
        queue.terminate()
    }
  }
}