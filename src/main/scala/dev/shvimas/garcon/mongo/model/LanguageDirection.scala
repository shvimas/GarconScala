package dev.shvimas.garcon.mongo.model

import dev.shvimas.garcon.mongo.model.{LanguageDirectionFields => Fields}
import dev.shvimas.garcon.translate.LanguageCode
import dev.shvimas.garcon.translate.LanguageCode.LanguageCode
import org.mongodb.scala.bson.annotations.BsonProperty

case class LanguageDirection(@BsonProperty(Fields.source) source: LanguageCode,
                             @BsonProperty(Fields.target) target: LanguageCode)

case object LanguageDirection {
  import LanguageCode._

  val EN_RU = LanguageDirection(EN, RU)
  val RU_EN = LanguageDirection(RU, EN)

  val default: LanguageDirection = EN_RU
}
