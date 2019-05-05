import sbt._

object Version {
  val scala = "2.12.8"

  val catsCore = "1.6.0"
  val akkaActor = "2.5.21"

  val json4s = "3.6.5"
  val sttpCore = "1.5.11"
  val mongoDriver = "2.6.0"
  val config = "1.3.3"

  val logging = "3.9.2"
  val logback = "1.2.3"
  val pprint = "0.5.3"

  val scalaTest = "3.0.5"
}

object Dependencies {
  val all: Seq[ModuleID] =
    Seq(
      "org.scala-lang" % "scala-reflect" % Version.scala,
      "org.typelevel" %% "cats-core" % Version.catsCore,
      "com.typesafe.akka" %% "akka-actor" % Version.akkaActor,
      "org.json4s" %% "json4s-native" % Version.json4s,
      "org.json4s" %% "json4s-ext" % Version.json4s,
      "com.softwaremill.sttp" %% "core" % Version.sttpCore,
      "org.mongodb.scala" %% "mongo-scala-driver" % Version.mongoDriver,
      "com.typesafe" % "config" % Version.config,
      "com.typesafe.scala-logging" %% "scala-logging" % Version.logging,
      "ch.qos.logback" % "logback-classic" % Version.logback,
      "com.lihaoyi" %% "pprint" % Version.pprint,
      "org.scalatest" %% "scalatest" % Version.scalaTest % Test,
    )
}
