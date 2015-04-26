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
  val persistentArguments:Seq[String] 

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
  val persistentArguments:Seq[String] 
}

case class SubCommand(client: Client, subProgram: Seq[String], subPersistentArguments: Seq[String]) extends ExecutableStandAlone{
  val program: Seq[String] = client.program++client.persistentArguments++subProgram
  val persistentArguments: Seq[String] = subPersistentArguments
}

object SubCommand {
  def apply(client: Client, subProgram: String, subPersistentArguments: Seq[String] = Nil): SubCommand = {
    SubCommand(client, Seq(subProgram), subPersistentArguments)
  }
}
