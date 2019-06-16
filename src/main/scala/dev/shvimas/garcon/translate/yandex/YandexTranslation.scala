package dev.shvimas.garcon.translate.yandex

import dev.shvimas.garcon.translate.Translation
import org.json4s.{DefaultFormats, Formats}
import org.json4s.native.JsonMethods.parse

import scala.util.Try

case class YandexTranslation(json: YandexTranslationJsonRepr, text: String)
    extends Translation {
  override val translatedText: String = json.text.mkString("; ")
  override val originalText: String = text
  override val translatorName: String = YandexTranslator.name
}

case class YandexTranslationJsonRepr(code: Int,
                                     lang: String,
                                     text: List[String])

object YandexTranslation {

  private implicit val formats: Formats = DefaultFormats

  def fromJson(json: String, text: String): Try[YandexTranslation] = {
    Try(parse(json).extract[YandexTranslationJsonRepr])
      .map(YandexTranslation(_, text))
  }
}
