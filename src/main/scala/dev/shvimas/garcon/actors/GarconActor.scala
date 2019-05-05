package dev.shvimas.garcon.actors

import akka.actor._
import cats.syntax.show._
import dev.shvimas.garcon.actors.MessageSender.SendMessage
import dev.shvimas.garcon.actors.TranslatorActor.TranslationRequest
import dev.shvimas.garcon.mongo.MongoOps
import dev.shvimas.garcon.mongo.model.LanguageDirection
import dev.shvimas.garcon.telegram.model.Update
import dev.shvimas.garcon.utils.ExceptionUtils.showThrowable
import dev.shvimas.garcon.utils.FutureUtils._

import scala.util.{Failure, Success}

class GarconActor extends Actor with ActorLogging with MongoOps {

  private val translator =
    context.actorOf(Props[TranslatorActor], "translatorActor")
  private val messageSender =
    context.actorOf(Props[MessageSender], "messageSender")

  override def receive: Receive = {
    case update @ Update(_, Some(message), None) =>
      message.text match {
        case Some(text) =>
          update.chatId match {
            case Some(chatId) =>
              val langDirection: LanguageDirection =
                getUserData(chatId).awaitResult() match {
                  case Success(None) =>
                    log.info(
                      s"No user data for $chatId found, setting defaults"
                    )
                    setDefaultUserData(chatId)
                    LanguageDirection.default
                  case Success(Some(userData)) =>
                    userData.languageDirection match {
                      case Some(languageDirection) =>
                        languageDirection
                      case None =>
                        setLangDirection(chatId, LanguageDirection.default)
                        LanguageDirection.default
                    }
                  case Failure(exception) =>
                    log.warning(s"Got ${exception.show}")
                    LanguageDirection.default
                }
              lookUpText(text, langDirection, chatId).awaitResult() match {
                case Success(None) =>
                  // FIXME: translator should return value and all communication should be via GarconActor
                  translator ! TranslationRequest(message.chat, text)
                // TODO: add to DB
                case Success(Some(commonTranslation)) =>
                  val translationOrCacheError: Option[String] =
                    commonTranslation.translation.orElse {
                      deleteText(commonTranslation.text, langDirection, chatId)
                      val msg = s"Cache error: no translation found for $text"
                      log.warning(msg)
                      Some(msg)
                    }
                  messageSender ! SendMessage(chatId, translationOrCacheError)

                case Failure(exception) =>
                  log.warning(
                    s"Failed to get cached translation for $text: got $exception"
                  )
                  translator ! TranslationRequest(message.chat, text)
                // TODO: add to DB
              }

            case None =>
              log.warning(s"Got $update without chatId")
          }

        case None =>
          log.warning(s"Got message ($message) without text")
      }

    case Update(id, None, Some(callbackQuery)) =>
      log.info(s"Got $callbackQuery from $id")
  }
}
