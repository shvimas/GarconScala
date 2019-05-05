package dev.shvimas.garcon.telegram.model

case class Chat(id: Int,
                `type`: String,
                title: Option[String],
                username: Option[String],
                firstName: Option[String],
                lastName: Option[String])
