package dev.shvimas.garcon.telegram.model

case class CallbackQuery(id: String,
                         from: User,
                         message: Option[Message],
                         inlineMessageId: Option[String],
                         data: Option[String])
