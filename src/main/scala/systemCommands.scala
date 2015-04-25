package com.billding

import scala.sys.process._
import scala.sys.process.Process
import scala.sys.process.ProcessBuilder

trait Executable {
  def execute(arguments: Seq[String]): String = {
    arguments!!
  }
}

trait Client extends Executable{

  val program:Seq[String]
  val commonArguments:Seq[String] 

  override def execute(arguments: Seq[String]): String  = {
    super.execute(program++commonArguments++arguments)
  }

}

//trait SubCommand extends Client

case class SubCommand(programBase: Seq[String], subProgram: String) extends Client{
  val program: Seq[String] = programBase++Seq(subProgram)

  val commonArguments = Nil
  def execute(): String  = {
    execute(program)
  }

}
