package dev.shvimas.garcon.actors

import akka.actor.typed._
import akka.actor.typed.scaladsl._
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.Scheduler
import akka.util.Timeout
import cats.syntax.show._
import dev.shvimas.garcon.actors.MessageSender.SendMessage
import dev.shvimas.garcon.actors.MongoActor._
import dev.shvimas.garcon.actors.TranslatorActor._
import dev.shvimas.garcon.mongo.model.LanguageDirection
import dev.shvimas.garcon.telegram.model.Update
import dev.shvimas.garcon.utils.ExceptionUtils.showThrowable

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object GarconActor {
  def apply(): Behavior[Update] =
    Behaviors.setup(new GarconActor(_))
}

class GarconActor(context: ActorContext[Update])
    extends AbstractBehavior[Update] {

  private val log = context.log

  private implicit val executor: ExecutionContextExecutor =
    context.executionContext
  implicit val scheduler: Scheduler = context.system.scheduler

  private val messageSender =
    context.spawn(MessageSender(), "messageSender")

  private val yandexTranslator =
    context.spawn(TranslatorActor.yandexTranslator(), "Yandex_translator")

  private val mongoActor =
    context.spawn(MongoActor(), "mongoActor")

  override def onMessage(msg: Update): Behavior[Update] = {
    msg match {
      case Update(_, Some(message), None) =>
        message.text match {
          case Some(text) =>
            msg.chatId match {
              case Some(chatId) =>
                respondToMessage(text, chatId)
              case None =>
                log.warning(s"Got $msg without chatId")
            }
          case None =>
            log.warning(s"Got message ($message) without text")
        }
      case Update(id, None, Some(callbackQuery)) =>
        log.info(s"Got $callbackQuery from $id")
      case Update(id, None, None) =>
        log.warning(s"Got update with neither message nor callback from $id")
      case update @ Update(id, Some(_), Some(_)) =>
        log.warning(s"""Got update with both message and callback from $id: 
             |$update
             """.stripMargin)
    }
    Behaviors.same
  }

  private def respondToMessage(text: String, chatId: Int): Unit = {
    implicit val timeout: Timeout = Timeout(10.seconds)

    mongoActor
      .ask[Try[LanguageDirection]](GetLanguageDirection(chatId, _))
      .onComplete { result: Try[Try[LanguageDirection]] =>
        result.flatten match {
          case Success(ld) =>
            val languageDirection = ld.maybeReverse(text)
            translate(text, chatId, languageDirection)
          case Failure(exception) =>
            log.error(exception.show)
        }
      }
  }

  private def translate(text: String,
                        chatId: Int,
                        languageDirection: LanguageDirection): Unit = {
    implicit val timeout: Timeout = Timeout(10.seconds)

    val translatorResult: Future[TranslationResponse] =
      yandexTranslator.ask[TranslationResponse](
        TranslationRequest(text, languageDirection, _)
      )

    translatorResult.onComplete { result: Try[TranslationResponse] =>
      val answer: String =
        result match {
          case Success(TranslationResponse(_, tried)) =>
            tried match {
              case Success(translation) =>
                mongoActor ! AddTranslation(translation, languageDirection, chatId)
                translation.translatedText
              case Failure(exception) =>
                log.error(exception.show)
                exception.show
            }
          case Failure(exception) =>
            log.error(exception.show)
            exception.show
        }

      messageSender ! SendMessage(chatId, Some(answer))
    }
  }
}
