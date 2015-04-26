package com.billding.git

import java.nio.file.Path
import java.nio.file.Paths

import com.billding.{Client, SubCommand}

import scala.language.postfixOps

case class Repo(path: Path, home: Path) extends Client {
  val program = Seq("git")
  val persistentArguments = Seq("--git-dir="+dir+".git")

  def fileName(): String = {
    path.toString.replaceAll("/","_")
  }
  def dir(): String = {
    home + "/" + path.toString + "/"
  }

  def firstWord(x: String) = x.split("\\s")(0)

  val showCommand = SubCommand(this,"show")

  val logCommand = SubCommand(this,"log")

  val todayCommand = SubCommand(this,"today")

  val branchCommand = SubCommand(this,"branch")

  val statusCommand = SubCommand(this,"status")
  def status = statusCommand.execute()

  // I'm going to keep this a def instead of a val, because even though it has no paramaters, 
  // it's still executing an external command with results that could change.
  def hashes: List[GitHash] = {

    val logOutput: Array[String] = logCommand.execute(Seq("--oneline")).split("\n")
    
    logOutput map { x =>  GitHash(firstWord(x))} toList
  }

} 

object Repo {
  def apply(pathString: String, homeString: String): Repo = {
    Repo( Paths.get(pathString), Paths.get(homeString))
  }
}
