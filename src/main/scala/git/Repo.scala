package com.billding.git

import java.nio.file.Path
import java.nio.file.Paths

case class Repo(path: Path, home: Path) {
  def fileName(): String = {
    path.toString.replaceAll("/","_")
  }
  def dir(): String = {
    home + "/" + path.toString + "/"
  }

  implicit val program = Seq("git")


  // I'm going to keep this a def instead of a val, because even though it has no paramaters, 
  // it's still executing an external command with results that could change.
  def hashes: List[GitHash] = {
    val loggerArguments = Seq("--git-dir="+dir+".git", "log", "--oneline")
    val logOutput: Array[String] = com.billding.SystemCommands.runFullCommand(loggerArguments).split("\n")

    logOutput map { x =>  GitHash(x.split("\\s")(0))} toList
  }

}
object Repo {
  def apply(pathString: String, homeString: String) = {
    new Repo( Paths.get(pathString), Paths.get(homeString))
  }
}
