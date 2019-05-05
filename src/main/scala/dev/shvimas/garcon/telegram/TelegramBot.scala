package dev.shvimas.garcon.telegram

import java.util.concurrent.TimeUnit

import com.softwaremill.sttp._
import com.typesafe.scalalogging.StrictLogging
import dev.shvimas.garcon.telegram.model.Result._
import org.json4s.{DefaultFormats, Formats}
import org.json4s.native.JsonMethods.parse

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

case class ApiRequestError(message: String) extends Exception(message)

trait TelegramBot extends StrictLogging {
  protected val settings: TelegramBotSettings

  implicit val defaultBackend: SttpBackend[Id, Nothing] =
    HttpURLConnectionBackend(
      SttpBackendOptions(
        FiniteDuration(20, TimeUnit.SECONDS),
        settings.proxy.map(_.toSttpBackendProxy)
      )
    )

  implicit val defaultFormats: DefaultFormats.type = DefaultFormats

  def callApi[R](method: String, params: Map[String, Any])(
    implicit formats: Formats = defaultFormats,
    manifest: Manifest[R]
  ): Try[R] = {
    sttp
      .get(uri"https://api.telegram.org/bot${settings.token}/$method?$params")
      .send()
      .body
      .left
      .map(ApiRequestError)
      .toTry
      .flatMap(s => Try(parse(s).camelizeKeys.extract[R]))
  }

  def getMe: Try[GetMeResult] =
    callApi[GetMeResult]("getMe", Map.empty)

  def getUpdates(offset: Long): Try[GetUpdatesResult] = {
    val params = Map("offset" -> offset)
    callApi[GetUpdatesResult]("getUpdates", params)
  }

  def sendMessage(
    chatId: Int,
    text: Option[String],
    disableNotification: Boolean = true
  ): Try[SendMessageResult] = {
    val params = Map(
      "chat_id" -> chatId,
      "text" -> text,
      "disable_notification" -> disableNotification.toString
    )
    logger.debug("Sending message")
    val triedSendMessageResult = callApi[SendMessageResult]("sendMessage", params)
    logger.debug("Done.")
    triedSendMessageResult
  }

  def deleteMessage(chatId: Int, messageId: Int): Try[BooleanResult] = {
    val params = Map("chat_id" -> chatId, "message_id" -> messageId)
    callApi[BooleanResult]("deleteMessage", params)
  }

  def answerCallbackQuery(queryId: String, text: String): Try[BooleanResult] = {
    val params = Map("query_id" -> queryId, "text" -> text)
    callApi[BooleanResult]("answerCallbackQuery", params)
  }

}
