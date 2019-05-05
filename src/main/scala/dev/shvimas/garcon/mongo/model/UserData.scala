package dev.shvimas.garcon.mongo.model

import dev.shvimas.garcon.mongo.model.{UserDataFields => Fields}
import org.mongodb.scala.bson.annotations.BsonProperty

case class UserData(
  @BsonProperty(Fields.chatId) chatId: Int,
  @BsonProperty(Fields.langDir) languageDirection: Option[LanguageDirection],
  @BsonProperty(Fields.translator) translator: Option[String]
)
