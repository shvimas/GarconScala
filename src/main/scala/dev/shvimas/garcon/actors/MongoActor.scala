package dev.shvimas.garcon.actors

import akka.actor.typed._
import akka.actor.typed.scaladsl._
import cats.syntax.show._
import dev.shvimas.garcon.actors.MongoActor.Request
import dev.shvimas.garcon.mongo.model.{LanguageDirection, UserData}
import dev.shvimas.garcon.mongo.MongoOps
import dev.shvimas.garcon.translate.Translation
import dev.shvimas.garcon.utils.ExceptionUtils.showThrowable

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success, Try}

object MongoActor {
  sealed trait Request

  case class GetLanguageDirection(chatId: Int,
                                  replyTo: ActorRef[Try[LanguageDirection]])
      extends Request

  case class AddTranslation(translation: Translation,
                            languageDirection: LanguageDirection,
                            chatId: Int)
      extends Request

//  case class LookUpText(text: String,
//                        languageDirection: LanguageDirection,
//                        chatId: Int)
//      extends Request
//
//  case class DeleteText(text: String,
//                        languageDirection: LanguageDirection,
//                        chatId: Int)
//      extends Request

  def apply(): Behavior[Request] =
    Behaviors.setup(context => new MongoActor(context))
}

class MongoActor(context: ActorContext[Request])
    extends AbstractBehavior[Request]
    with MongoOps {

  import MongoActor._

  private val log = context.log

  private implicit val ec: ExecutionContextExecutor = context.executionContext

  override def onMessage(msg: Request): Behavior[Request] = {
    msg match {
      case GetLanguageDirection(chatId, replyTo) =>
        getUserData(chatId).onComplete { result: Try[Option[UserData]] =>
          val triedLanguageDirection: Try[LanguageDirection] =
            result.map {
              case None =>
                log.info(s"No user data for $chatId found, setting defaults")
                setDefaultUserData(chatId)
                LanguageDirection.default
              case Some(userData) =>
                userData.languageDirection match {
                  case Some(languageDirection) =>
                    languageDirection
                  case None =>
                    log.info(
                      s"No language direction for $chatId found, setting default"
                    )
                    setLangDirection(chatId, LanguageDirection.default)
                    LanguageDirection.default
                }
            }

          replyTo ! triedLanguageDirection
        }
      case request @ AddTranslation(translation, languageDirection, chatId) =>
        addText(translation, languageDirection, chatId).onComplete {
          case Success(updateResult) =>
            if (!updateResult.wasAcknowledged()) {
              log.error(s"$request was not acknowledged")
            }
          case Failure(exception) =>
            log.error(exception.show)
        }
    }
    Behaviors.same
  }
}
