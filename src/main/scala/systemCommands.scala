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

case class SubCommand(client: Client, subProgram: String) extends Executable{
  val program: Seq[String] = client.program++client.commonArguments++Seq(subProgram)

  val commonArguments = Nil
  def execute(): String  = {
    execute(Nil)
  }

  override def execute(arguments: Seq[String]): String = {
    println(s"Command: ${program++arguments}")
    program++arguments!!
  }

}
