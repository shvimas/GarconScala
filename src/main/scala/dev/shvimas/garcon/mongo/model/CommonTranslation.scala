package dev.shvimas.garcon.mongo.model

import dev.shvimas.garcon.mongo.model.{CommonTranslationFields => Fields}
import org.mongodb.scala.bson.annotations.BsonProperty

case class CommonTranslation(
  @BsonProperty(Fields.text) text: String,
  @BsonProperty(Fields.abbyy) abbyy: Option[String],
  @BsonProperty(Fields.yandex) yandex: Option[String]
) {
  def translation: Option[String] =
    abbyy.orElse(yandex)
}
