package dev.shvimas.garcon.mongo

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.mongodb.scala.{Observable, Observer}

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class AwaitableMany[T](observable: Observable[T]) extends Observable[T] {
  override def subscribe(observer: Observer[_ >: T]): Unit =
    observable.subscribe(observer)

  def awaitResult(timeout: Duration = 10.minutes): Try[List[T]] = {
    var results: List[T] = Nil
    var errorOpt: Option[Throwable] = None
    var completed = false

    val latch = new CountDownLatch(1)

    val observer: Observer[T] = new Observer[T] {
      override def onNext(result: T): Unit = {
        results +:= result
      }

      override def onError(e: Throwable): Unit = {
        errorOpt = Some(e)
        latch.countDown()
        completed = true
      }

      override def onComplete(): Unit = {
        latch.countDown()
        completed = true
      }
    }

    subscribe(observer)
    latch.await(timeout.toMillis, TimeUnit.MILLISECONDS)

    if (!completed) {
      return Failure(TimedOutObservable(timeout))
    }

    errorOpt match {
      case Some(throwable) if results.nonEmpty =>
        Failure(InconsistentStateFromObservable(results, throwable))
      case Some(throwable) =>
        Failure(throwable)
      case None =>
        Success(results)
    }
  }
}

object AwaitableMany {
  def toAwaitable[T](observable: Observable[T]): AwaitableMany[T] =
    new AwaitableMany(observable)
}
