package dev.shvimas.garcon.actors

import akka.actor._
import dev.shvimas.garcon.actors.MessageSender.SendMessage
import dev.shvimas.garcon.mongo.model.LanguageDirection
import dev.shvimas.garcon.telegram.model.Chat
import dev.shvimas.garcon.translate.abbyy.AbbyyTranslator

import scala.util.{Failure, Success}

object TranslatorActor {
  case class TranslationRequest(chat: Chat,
                                text: String,
                                languageDirection: LanguageDirection)
  case class TranslationResponse(chat: Chat,
                                 text: String,
                                 translation: String,
                                 languageDirection: LanguageDirection)
}

class TranslatorActor extends Actor with ActorLogging {
  import TranslatorActor._

  private val translator = AbbyyTranslator()

  private val messageSender =
    context.actorOf(Props[MessageSender], "messageSender")

  override def receive: Receive = {
    case TranslationRequest(chat, text, languageDirection) =>
      val tryTranslation =
        translator.translate(
          text = text,
          srcLangCode = languageDirection.source,
          dstLangCode = languageDirection.target
        )

      tryTranslation match {
        case Success(translation) =>
          messageSender ! SendMessage(
            chatId = chat.id,
            text = Some(translation.translatedText)
          )
        case Failure(exception) =>
          messageSender ! SendMessage(
            chatId = chat.id,
            text = Some(s"got error while translating: $exception")
          )
          log.error(s"$exception")
      }
    // TODO: send default menu
  }
}
