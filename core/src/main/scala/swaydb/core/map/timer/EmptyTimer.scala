package swaydb.core.map.timer

import swaydb.core.data.Time

object EmptyTimer extends Timer {
  override def next: Time =
    Time.empty

  override def close: Unit =
    ()
}
