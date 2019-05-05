package dev.shvimas.garcon.mongo.model

import org.mongodb.scala.bson.annotations.BsonProperty

case class Globals(@BsonProperty(GlobalsFields.offset) offset: Option[Long])
