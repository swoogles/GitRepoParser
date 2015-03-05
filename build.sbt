name := "hello"

version := "1.0"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "io.argonaut" %% "argonaut" % "6.0.4",
  "org.scalanlp" % "breeze_2.10" % "0.9",
  // native libraries are not included by default. add this if you want them (as of 0.7)
  // native libraries greatly improve performance, but increase jar sizes.
  "org.scalanlp" % "breeze-natives_2.10" % "0.9",
  "org.scalanlp" % "breeze-viz_2.10" % "0.5.1",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9"
  )

org.scalastyle.sbt.ScalastylePlugin.Settings

mainClass := Some("GitManager")

