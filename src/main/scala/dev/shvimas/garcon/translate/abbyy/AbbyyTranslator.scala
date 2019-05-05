package dev.shvimas.garcon.translate.abbyy

import com.softwaremill.sttp._
import dev.shvimas.garcon.translate.{LanguageCode, Translator}
import dev.shvimas.garcon.MainResources.config
import dev.shvimas.garcon.translate.LanguageCode.LanguageCode

import scala.util.{Failure, Success, Try}

case class AbbyyRequestError(response: Response[String])
    extends Exception(s"$response")

class AbbyyTranslator extends Translator {
  import AbbyyTranslator._

  override type LanguageCodeImpl = Int
  private val tokenLock = new Object
  private var _cachedToken: Try[String] = newToken

  override def translateImpl(text: String,
                             srcLang: StatusCode,
                             dstLang: StatusCode): Try[AbbyyTranslation] = {
    def translationRequest(token: String): Try[AbbyyTranslation] = {
      val response =
        sttp
          .get(
            uri"$baseUrl/api/v1/Minicard?text=$text&srcLang=$srcLang&dstLang=$dstLang"
          )
          .header("Authorization", f"Bearer $token")
          .contentLength(0)
          .send()

      responseToTry(response)
        .flatMap(AbbyyTranslation.fromString)
    }

    cachedToken.flatMap(translationRequest) match {
      case Failure(AbbyyRequestError(response))
          if response.code == StatusCodes.Unauthorized =>
        cachedToken = newToken
        cachedToken.flatMap(translationRequest)
      case Failure(exception) =>
        Failure(exception)
      case Success(value) =>
        Success(value)
    }
  }

  private def cachedToken: Try[String] = _cachedToken

  //noinspection ScalaUnusedSymbol
  private def cachedToken_=(newValue: Try[String]): Unit =
    tokenLock.synchronized(_cachedToken = newValue)

  private[abbyy] def newToken: Try[String] = {
    val response = sttp
      .post(uri"$baseUrl/api/v1.1/authenticate")
      .header("Authorization", f"Basic $apiKey")
      .body("")
      .send()

    responseToTry(response)
  }

  private def responseToTry(response: Response[String]): Try[String] = {
    val errorOrBody: Either[Exception, String] =
      response.body match {
        case Left(_)  => Left(AbbyyRequestError(response))
        case Right(r) => Right(r)
      }

    errorOrBody.toTry
  }
  override def toLanguageCodeImpl(languageCode: LanguageCode): StatusCode =
    languageCode match {
      case LanguageCode.EN => 1033
      case LanguageCode.RU => 1049
    }
}

object AbbyyTranslator {
  implicit val backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()

  private val baseUrl = "https://developers.lingvolive.com"

  private val apiKey = config.getString("abbyy.apiKey")

  def apply(): AbbyyTranslator = new AbbyyTranslator()
}
