package dev.shvimas.garcon.telegram.model

case class MessageEntity(`type`: String,
                         offset: Int,
                         length: Int,
                         url: Option[String],
                         user: Option[User])
