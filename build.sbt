name := "Garcon"

version := "0.1-SNAPSHOT"

scalaVersion := Version.scala

libraryDependencies ++= Dependencies.all

scalacOptions ++=
  Seq("-feature", "-Xfatal-warnings", "-Ypartial-unification")

mainClass in Compile := Some("dev.shvimas.garcon.Main")

//enablePlugins(JavaAppPackaging)
//enablePlugins(DockerPlugin)
//enablePlugins(AshScriptPlugin) // for alpine-based image

//dockerBaseImage := "openjdk:jre"
//dockerBaseImage := "openjdk:jre-alpine"

enablePlugins(DockerPlugin)
dockerfile in docker := {
  val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value
  val jarTarget = s"/app/${jarFile.getName}"

  val mainClazz = mainClass
    .in(Compile, packageBin)
    .value
    .getOrElse(sys.error("Expected exactly one main class"))

  val classpath = (managedClasspath in Compile).value
  val classpathString = classpath.files
    .map("/app/" + _.getName)
    .mkString(":") + ":" + jarTarget
  
  new Dockerfile {
    // Base image
    from("openjdk:jre-alpine")
    // Add all files on the classpath
    add(classpath.files, "/app/")
    // Add the JAR file
    add(jarFile, jarTarget)
    // On launch run Java with the classpath and the main class
    entryPoint("java", "-cp", classpathString, mainClazz)
  }
}
