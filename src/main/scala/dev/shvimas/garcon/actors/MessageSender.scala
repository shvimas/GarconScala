package dev.shvimas.garcon.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl._
import dev.shvimas.garcon.MainResources

object MessageSender {
  sealed trait Request
  case class SendMessage(chatId: Int, text: Option[String]) extends Request

  def apply(): Behavior[Request] =
    Behaviors.setup(new MessageSender(_))
}

class MessageSender(context: ActorContext[MessageSender.Request])
    extends AbstractBehavior[MessageSender.Request] {
  import MessageSender._

  override def onMessage(msg: Request): Behavior[Request] = {
    msg match {
      case SendMessage(chatId, text) =>
        MainResources.bot.sendMessage(chatId = chatId, text = text)
        context.log.info(s"$msg")
    }
    Behaviors.same
  }
}
