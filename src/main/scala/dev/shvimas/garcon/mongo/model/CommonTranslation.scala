package dev.shvimas.garcon.mongo.model

import dev.shvimas.garcon.mongo.model.{CommonTranslationFields => Fields}
import dev.shvimas.garcon.translate.Translation
import org.mongodb.scala.bson.annotations.BsonProperty

case class CommonTranslation(
  @BsonProperty(Fields.text) text: String,
  @BsonProperty(Fields.abbyy) abbyy: Option[String],
  @BsonProperty(Fields.yandex) yandex: Option[String]
) extends Translation {

  def translation: Option[String] =
    abbyy.orElse(yandex)

  override val translatedText: String = translation.getOrElse("———")
  override val originalText: String = text
  override val translatorName: String =
    abbyy -> yandex match {
      case (Some(_), Some(_)) => "abbyy and yandex"
      case (Some(_), None)    => "abbyy"
      case (None, Some(_))    => "yandex"
      case (None, None)       => "unknown source"
    }
}
