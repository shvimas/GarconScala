package dev.shvimas.garcon.mongo

import com.mongodb.ConnectionString
import dev.shvimas.garcon.MainResources.config
import dev.shvimas.garcon.mongo.model._
import dev.shvimas.garcon.translate._
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala._
import org.mongodb.scala.bson._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.UpdateOptions
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.result._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// do not use Lazy/Strict Logging here as MongoOps is used in actors which should have their own logger
trait MongoOps {
  import MongoOps._

  protected val garconDb: MongoDatabase = client.getDatabase(DbName.garcon)

  protected val globalsColl: MongoCollection[Globals] =
    garconDb.getCollection(CollName.globals)

  protected val usersDataColl: MongoCollection[UserData] =
    garconDb.getCollection(CollName.usersData)

  def getGlobals: Future[Option[Globals]] =
    globalsColl
      .find()
      .first()
      .toFutureOption()

  def updateOffset(offset: Int): Future[UpdateResult] =
    globalsColl
      .updateOne(
        filter = emptyBson,
        update = max(GlobalsFields.offset, offset),
        options = upsert
      )
      .toFuture()

  def getOffset: Future[Long] =
    getGlobals map {
      case Some(globals) =>
        globals.offset.getOrElse(0)
      case None =>
        updateOffset(0)
        0
    }

  private def getWordsColl(langDirection: LanguageDirection,
                           chatId: Int): MongoCollection[CommonTranslation] =
    garconDb.getCollection(
      s"${chatId}_${langDirection.source}-${langDirection.target}"
    )

  def lookUpText(text: String,
                 langDirection: LanguageDirection,
                 chatId: Int): Future[Option[CommonTranslation]] = {
    getWordsColl(langDirection, chatId)
      .find(filter = equal(CommonTranslationFields.text, text))
      .first()
      .toFutureOption()
  }

  def addText(text: String,
              translation: Translation,
              langDirection: LanguageDirection,
              chatId: Int): Future[UpdateResult] = {
    getWordsColl(langDirection, chatId)
      .updateOne(
        filter = equal(CommonTranslationFields.text, text),
        update = combine(
          set(translation.translatorName, translation.translatedText),
          set(CommonTranslationFields.text, text)
        ),
        options = upsert
      )
      .toFuture()
  }

  def deleteText(text: String,
                 langDirection: LanguageDirection,
                 chatId: Int): Future[DeleteResult] = {
    getWordsColl(langDirection, chatId)
      .deleteOne(filter = equal(CommonTranslationFields.text, text))
      .toFuture()
  }

  def setLangDirection(
    chatId: Int,
    langDirection: LanguageDirection
  ): Future[UpdateResult] = {
    usersDataColl
      .updateOne(
        filter = equal(UserDataFields.chatId, chatId),
        update = set(UserDataFields.langDir, langDirection),
        options = upsert
      )
      .toFuture()
  }

  def getUserData(chatId: Int): Future[Option[UserData]] = {
    usersDataColl
      .find(filter = equal(UserDataFields.chatId, chatId))
      .first()
      .toFutureOption()
  }

  def setDefaultUserData(chatId: Int): Future[Completed] = {
    usersDataColl
      .insertOne(UserData(chatId, Some(LanguageDirection.default), None))
      .toFuture()
  }
}

object MongoOps {
  protected val username: String = config.getString("mongo.username")
  protected val password: String = config.getString("mongo.password")
  protected val host: String = config.getString("mongo.host")
  protected val port: Int = config.getInt("mongo.port")

  private val connectionString = new ConnectionString(
    s"mongodb://$username:$password@$host:$port"
  )

  private val caseClassCodecs =
    fromProviders(
      classOf[Globals],
      classOf[UserData],
      classOf[CommonTranslation],
      classOf[LanguageDirection],
      LanguageCode.MongoCodecProvider
    )

  private val codecRegistry =
    fromRegistries(DEFAULT_CODEC_REGISTRY, caseClassCodecs)

  private val clientSettings =
    MongoClientSettings
      .builder()
      .applyConnectionString(connectionString)
      .codecRegistry(codecRegistry)
      .build()

  protected val client = MongoClient(clientSettings)

  protected val emptyBson = BsonDocument()

  protected val upsert: UpdateOptions = new UpdateOptions().upsert(true)

  protected object CollName {
    val globals = "globals"
    val usersData = "users_data"
  }

  protected object DbName {
    val garcon = "garcon"
  }
}
