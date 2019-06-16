package dev.shvimas.garcon.actors

import akka.actor.typed._
import akka.actor.typed.scaladsl._
import dev.shvimas.garcon.actors.TranslatorActor.TranslationRequest
import dev.shvimas.garcon.mongo.model.LanguageDirection
import dev.shvimas.garcon.translate.{Translation, Translator}
import dev.shvimas.garcon.translate.abbyy.AbbyyTranslator
import dev.shvimas.garcon.translate.yandex.YandexTranslator

import scala.util.Try

object TranslatorActor {
  case class TranslationRequest(text: String,
                                languageDirection: LanguageDirection,
                                respondTo: ActorRef[TranslationResponse])

  case class TranslationResponse(text: String, translation: Try[Translation])

  def abbyyTranslator(): Behavior[TranslationRequest] =
    Behaviors.setup(new TranslatorActor(_, AbbyyTranslator()))

  def yandexTranslator(): Behavior[TranslationRequest] =
    Behaviors.setup(new TranslatorActor(_, new YandexTranslator))
}

class TranslatorActor(context: ActorContext[TranslationRequest],
                      translator: Translator)
    extends AbstractBehavior[TranslationRequest] {
  import TranslatorActor._

  override def onMessage(
    msg: TranslationRequest
  ): Behavior[TranslationRequest] = {
    msg match {
      case TranslationRequest(text, languageDirection, respondTo) =>
        val triedTranslation =
          translator.translate(
            text = text,
            srcLangCode = languageDirection.source,
            dstLangCode = languageDirection.target
          )
        respondTo ! TranslationResponse(text, triedTranslation)
    }
    Behaviors.same
  }
}
