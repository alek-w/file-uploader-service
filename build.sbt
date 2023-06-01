name := """file-uploader-service"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test
libraryDependencies += ws

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka_2.11" % "0.10.0.0"
)