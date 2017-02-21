name := "gitrepoparser"

version := "0.1-SNAPSHOT"

organization := "com.solutionreach"

scalaVersion := "2.12.1"

sbtVersion := "0.13.13"

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
//  "io.argonaut" %% "argonaut" % "6.2-SNAPSHOT",
  "io.argonaut" %% "argonaut" % "6.2-RC2" changing(),

"com.typesafe.akka" %% "akka-actor" % "2.4.17",
  "com.lihaoyi" %% "ammonite-ops" % "0.8.2"
)

org.scalastyle.sbt.ScalastylePlugin.Settings

scalacOptions ++= Seq("-feature")

mainClass := Some("GitManager")

