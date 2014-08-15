name := "hello"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "io.argonaut" %% "argonaut" % "6.0.4"     
)

mainClass := Some("RepoParser")
