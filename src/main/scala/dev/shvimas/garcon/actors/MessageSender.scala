package dev.shvimas.garcon.actors

import akka.actor._
import dev.shvimas.garcon.MainResources

object MessageSender {
  case class SendMessage(chatId: Int, text: Option[String])
}

class MessageSender extends Actor with ActorLogging {
  import MessageSender._

  override def receive: Receive = {
    case msg @ SendMessage(chatId, text) =>
      MainResources.bot.sendMessage(chatId = chatId, text = text)
      log.info(s"$sender() called $msg")
  }
}
