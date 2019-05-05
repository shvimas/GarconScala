package dev.shvimas.garcon.utils

import cats.Show

object ExceptionUtils {
  implicit val showThrowable: Show[Throwable] =
    (t: Throwable) => {
      val message = Option(t.getMessage).getOrElse("null")
      val causeMessage = Option(t.getCause) match {
        case Some(cause) => showThrowable.show(cause)
        case None        => "(no cause)"
      }
      s"$message $causeMessage"
    }
}
