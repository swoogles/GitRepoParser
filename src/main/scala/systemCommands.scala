package com.billding

import scala.sys.process._
import scala.sys.process.Process
import scala.sys.process.ProcessBuilder

trait Executable {
  def execute(arguments: Seq[String]): String = {
    arguments!!
  }
}

trait ExecutableStandAlone extends Executable {
  val program:Seq[String]
  val persistentArguments:Seq[String] 

  def execute(): String = {
    execute(program++persistentArguments)
  }
}

trait Client extends ExecutableStandAlone{
  val program:Seq[String]
  val persistentArguments:Seq[String] 

  override def execute(arguments: Seq[String]): String  = {
    super.execute(program++persistentArguments++arguments)
  }

}

case class SubCommand(client: Client, subProgram: String, subPersistentArguments: Seq[String] = Nil) extends ExecutableStandAlone{
  val program: Seq[String] = client.program++client.persistentArguments++Seq(subProgram)
  val persistentArguments: Seq[String] = subPersistentArguments
  def !!(): String = {
    execute
  }
}
