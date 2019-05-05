package dev.shvimas.garcon

import com.typesafe.scalalogging.StrictLogging
import dev.shvimas.garcon.telegram._
import dev.shvimas.garcon.MainResources.config

import scala.util.Try

class Bot(override protected val settings: TelegramBotSettings)
    extends TelegramBot

object Bot extends StrictLogging {
  private val proxySettings = Try {
    val proxyAuth = ProxyAuthUsernamePassword(
      username = config.getString("proxy.username"),
      password = config.getString("proxy.password")
    )
    SocksProxy(
      host = config.getString("proxy.host"),
      port = config.getInt("proxy.port"),
      auth = Some(proxyAuth)
    )
  }.toOption

  if (proxySettings.isEmpty) {
    logger.warn("Proxy settings not found!")
  }

  private def makeBot(token: String): Bot =
    new Bot(TelegramBotSettings(token, proxySettings))

  lazy val testBot: Bot = makeBot(config.getString("bot.testToken"))
  lazy val bot: Bot = makeBot(config.getString("bot.token"))
}
