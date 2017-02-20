name := "hello"

version := "1.0"

scalaVersion := "2.11.1"

sbtVersion := "0.13.13"

libraryDependencies ++= Seq(
  "io.argonaut" %% "argonaut" % "6.0.4",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.lihaoyi" %% "ammonite-ops" % "0.8.2"
)

org.scalastyle.sbt.ScalastylePlugin.Settings

scalacOptions ++= Seq("-feature")

mainClass := Some("GitManager")

