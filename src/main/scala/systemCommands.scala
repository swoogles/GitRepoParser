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

case class SubCommand(subProgram: Seq[String]) extends Client{
  val program = subProgram

  val commonArguments = Nil
  def execute(argument: String): String  = {
    execute(Seq(argument))
  }

}

//object SubCommand {
//  def apply(program: String) = {
      

object SystemCommands {
  def runFullCommand (arguments:Seq[String] )( implicit program:Seq[String]):String = {
    val fullCommand = (program++arguments)
    //Process(fullCommand).lineStream.last
    //ProcessBuilder(fullCommand).lineStream.toString
    fullCommand!!

    //fullCommand.run //Best version
  }

  // Wildcards aren't properly expanded if you try to feed them directly into 
  // Process arguments.
  def runFullCommandWithWildCards (arguments:Seq[String] )( implicit program:Seq[String]):String = {
    val fullCommand = (program++arguments)
    Process(Seq("bash", "-c", fullCommand.mkString(" "))).!!
  }
}

