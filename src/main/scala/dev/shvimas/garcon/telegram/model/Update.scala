package dev.shvimas.garcon.telegram.model

case class Update(updateId: Int,
                  message: Option[Message],
                  callbackQuery: Option[CallbackQuery]) {
  require(message.isEmpty || callbackQuery.isEmpty)

  def chatId: Option[Int] = {
    message.foreach(msg => return Some(msg.chat.id))
    callbackQuery.foreach(cb => return Some(cb.from.id))
    None
  }
}
