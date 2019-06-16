package dev.shvimas.garcon.translate.yandex

import com.softwaremill.sttp._
import dev.shvimas.garcon.translate.{Translation, Translator}
import dev.shvimas.garcon.translate.LanguageCode.LanguageCode
import dev.shvimas.garcon.MainResources.config

import scala.concurrent.duration._
import scala.util.Try

class YandexTranslator extends Translator {
  import YandexTranslator._

  override type LanguageCodeImpl = LanguageCode

  private val readTimeout: FiniteDuration = 5.seconds
  private implicit val backend: SttpBackend[Id, Nothing] =
    HttpURLConnectionBackend(
      SttpBackendOptions(connectionTimeout = readTimeout, proxy = None)
    )

  override def translateImpl(text: String,
                             srcLang: LanguageCode,
                             dstLang: LanguageCode): Try[Translation] = {
    val params: Map[String, String] =
      Map("key" -> apiKey, "lang" -> s"$srcLang-$dstLang", "text" -> text)

    sttp
      .get(uri"https://translate.yandex.net/api/v1.5/tr.json/translate?$params")
      .send()
      .body
      .left
      .map(ApiRequestError)
      .toTry
      .flatMap(YandexTranslation.fromJson(_, text))
  }

  override def toLanguageCodeImpl(languageCode: LanguageCode): LanguageCode =
    languageCode

  private case class ApiRequestError(message: String) extends Exception(message)
}

object YandexTranslator {
  val name = "yandex"

  private val apiKey = config.getString("yandex.apiKey")
}
