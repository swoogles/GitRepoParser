package com.billding

import scala.sys.process._
import scala.sys.process.Process
import scala.sys.process.ProcessBuilder

import scala.language.postfixOps

trait Executable {
  def execute(arguments: Seq[String]): String = {
    Process(Seq("bash", "-c", arguments.mkString(" "))).!! // Wildcard-safe version
  }
}

trait ExecutableStandAlone extends Executable{
  val program:Seq[String]
  def persistentArguments:Seq[String] 

  def execute(): String = {
    execute(Nil)
  }

  override def execute(arguments: Seq[String]): String = {
    super.execute(program++persistentArguments++arguments)
  }

  def !!(): String = {
    execute(Nil)
  }

  def fullCommand(): Seq[String] = program ++ persistentArguments
}

trait Client extends ExecutableStandAlone{
  val program:Seq[String]
  def persistentArguments:Seq[String] 
}

case class SubCommand(clientProgram: Seq[String], clientArguments: Seq[String], subProgram: Seq[String], subPersistentArguments: Seq[String]) extends ExecutableStandAlone{
  val program: Seq[String] = clientProgram++clientArguments++subProgram
  def persistentArguments: Seq[String] = subPersistentArguments
}

object SubCommand {
  def apply(clientProgram: Seq[String], clientArguments: Seq[String], subProgram: String, subPersistentArguments: Seq[String] = Nil): SubCommand = {
    SubCommand(clientProgram, clientArguments, Seq(subProgram), subPersistentArguments)
  }
}
