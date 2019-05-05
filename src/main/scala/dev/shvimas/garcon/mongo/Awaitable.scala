package dev.shvimas.garcon.mongo

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.mongodb.scala.{Observer, SingleObservable, Subscription}

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class Awaitable[T](observable: SingleObservable[T])
    extends SingleObservable[T] {
  override def subscribe(observer: Observer[_ >: T]): Unit =
    observable.subscribe(observer)

  def awaitResult(timeout: Duration = 10.minutes): Try[T] = {
    var resultOpt: Option[T] = None
    var manyResultsList: List[T] = Nil
    var errorOpt: Option[Throwable] = None
    var completed = false

    val latch = new CountDownLatch(1)

    val observer: Observer[T] = new Observer[T] {
      override def onNext(result: T): Unit = {
        resultOpt match {
          case Some(oldResult) =>
            resultOpt = None // to indicate that smth went wrong
            manyResultsList = manyResultsList match {
              case Nil  => List(result, oldResult)
              case list => result :: list
            }
          case None =>
            resultOpt = Some(result)
        }
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

    (resultOpt, errorOpt) match {
      case (Some(result), Some(throwable)) =>
        Failure(InconsistentStateFromSingleObservable(result, throwable))
      case (None, Some(throwable)) =>
        manyResultsList match {
          case Nil =>
            Failure(throwable)
          case results =>
            Failure(ManyResultsFromSingleObservable(results, throwable))
        }
      case (Some(result), None) =>
        Success(result)
      case (None, None) =>
        Failure(NoResultsFromSingleObservable())
    }
  }

  def map[S](s: T => S): Awaitable[S] =
    Awaitable.toAwaitable(
      (observer: Observer[_ >: S]) =>
        observable.subscribe(new Observer[T] {
          override def onError(throwable: Throwable): Unit =
            observer.onError(throwable)

          override def onSubscribe(subscription: Subscription): Unit =
            observer.onSubscribe(subscription)

          override def onComplete(): Unit = observer.onComplete()

          override def onNext(tResult: T): Unit = observer.onNext(s(tResult))
        })
    )

}

object Awaitable {
  def toAwaitable[T](singleObservable: SingleObservable[T]): Awaitable[T] =
    new Awaitable(singleObservable)
}
