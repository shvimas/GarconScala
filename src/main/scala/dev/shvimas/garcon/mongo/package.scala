package dev.shvimas.garcon

import org.mongodb.scala.{Observable, SingleObservable}

import scala.concurrent.duration.Duration
import scala.language.experimental.macros

package object mongo {
  object Implicits {
    implicit class CanBeAwaitableObservable[T](observable: Observable[T]) {
      def toAwaitable: AwaitableMany[T] =
        AwaitableMany.toAwaitable(observable)
    }

    implicit class CanBeSingleAwaitableObservable[T](
      singleObservable: SingleObservable[T]
    ) {
      def toAwaitable: Awaitable[T] =
        Awaitable.toAwaitable(singleObservable)
    }
  }

  case class NoResultsFromSingleObservable()
      extends Exception("no results from SingleObservable")

  case class ManyResultsFromSingleObservable[T](results: List[T],
                                                throwable: Throwable)
      extends Exception(
        s"many results from single observable: ${results.mkString(", ")} and throwable: $throwable"
      )

  case class TimedOutObservable(timeout: Duration)
      extends Exception(
        s"got neither result nor error from observable in $timeout"
      )

  case class InconsistentStateFromObservable[T](results: List[T],
                                                throwable: Throwable)
      extends Exception("got both result and error from observable")

  case class InconsistentStateFromSingleObservable[T](result: T,
                                                      throwable: Throwable)
      extends Exception("got both result and error from observable")
}
