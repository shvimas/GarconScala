import sbt._

object Versions {
  val json4sNativeVersion = "3.6.5"
  val mongoDriverVersion = "2.6.0"
}

object Dependencies {

  import Versions._

  val json4sNative = "org.json4s" %% "json4s-native" % json4sNativeVersion

  val mongoDriver = "org.mongodb.scala" %% "mongo-scala-driver" % mongoDriverVersion

  val all: Seq[ModuleID] = Seq(
    json4sNative,
    mongoDriver,
  )
}
