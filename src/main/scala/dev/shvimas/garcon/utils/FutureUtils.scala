package dev.shvimas.garcon.utils

import scala.concurrent.{Await, Awaitable}
import scala.concurrent.duration.Duration
import scala.concurrent.duration.Duration.Inf
import scala.language.implicitConversions
import scala.util.Try

object FutureUtils {
  class AwaitResult[T](awaitable: Awaitable[T]) {
    def awaitResult(timeout: Duration = Inf): Try[T] = {
      Try(Await.result(awaitable, timeout))
    }
  }

  implicit def awaitable2awaitResult[T](
    awaitable: Awaitable[T]
  ): AwaitResult[T] = new AwaitResult(awaitable)

}
