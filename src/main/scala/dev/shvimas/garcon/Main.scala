package dev.shvimas.garcon

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import dev.shvimas.garcon.actors.GarconActor
import dev.shvimas.garcon.mongo.MongoOps
import dev.shvimas.garcon.telegram.model.Update
import dev.shvimas.garcon.utils.FutureUtils._

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object Main extends App with StrictLogging with MongoOps {
  import MainResources._

  implicit val system: ActorSystem = ActorSystem("GarconBot")
  val garconActor = system.actorOf(Props[GarconActor], "garconActor")

  val minAskUpdatesPeriod = config.getInt("bot.min_period")
  val minWorkingDuration = Duration(minAskUpdatesPeriod, TimeUnit.MILLISECONDS)

  while (true) {
    val started = System.currentTimeMillis()

    val offset: Long =
      getOffset.awaitResult() match {
        case Success(value) =>
          value
        case Failure(exception) =>
          throw exception
      }

    bot.getUpdates(offset) match {
      case Success(getUpdatesResult) =>
        val maxUpdateId = getUpdatesResult.result.map(_.updateId).fold(-1)(math.max)
        if (maxUpdateId >= 0) {
          updateOffset(maxUpdateId + 1).awaitResult() match {
            case Success(res) =>
              require(res.getModifiedCount == 1, s"current update id: $maxUpdateId, offset: $offset")
            case Failure(exception) =>
              throw exception
          }
        }

        getUpdatesResult.result
          .groupBy(_.chatId)
          .values
          .par
          .foreach(_.foreach((update: Update) => garconActor ! update))

      case Failure(exception) =>
        logger.error(exception.toString)
        exception.printStackTrace()
    }

    Thread.sleep(math.max(0, minWorkingDuration.toMillis - started))
  }
}

object MainResources {
  lazy val config: Config = ConfigFactory.load("private/secrets.conf")
  lazy val bot: Bot = Bot.bot
}
