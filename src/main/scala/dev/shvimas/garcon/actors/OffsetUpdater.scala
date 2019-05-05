package dev.shvimas.garcon.actors

import akka.actor.Actor
import dev.shvimas.garcon.mongo.MongoOps

class OffsetUpdater extends Actor with MongoOps {
  override def receive: Receive = {
    case offset: Int => updateOffset(offset)
  }
}
