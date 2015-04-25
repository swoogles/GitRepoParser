package com.billding.git

import java.nio.file.Path
import java.nio.file.Paths

import com.billding.{Client, SubCommand}

case class Repo(path: Path, home: Path) extends Client {
  val program = Seq("git")
  val commonArguments = Seq("--git-dir="+dir+".git")

  def fileName(): String = {
    path.toString.replaceAll("/","_")
  }
  def dir(): String = {
    home + "/" + path.toString + "/"
  }

  def firstWord(x: String) = x.split("\\s")(0)

  val showCommand = SubCommand(program)
  def show() = showCommand.execute("show")

  // I'm going to keep this a def instead of a val, because even though it has no paramaters, 
  // it's still executing an external command with results that could change.
  def hashes: List[GitHash] = {
    val loggerArguments = Seq("log", "--oneline")
    val logOutput: Array[String] = execute(loggerArguments).split("\n")

    logOutput map { x =>  GitHash(firstWord(x))} toList
  }

} 

object Repo {
  def apply(pathString: String, homeString: String): Repo = {
    Repo( Paths.get(pathString), Paths.get(homeString))
  }
}
