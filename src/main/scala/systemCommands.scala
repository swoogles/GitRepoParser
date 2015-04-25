package com.billding

import scala.sys.process._
import scala.sys.process.Process
import scala.sys.process.ProcessBuilder

trait Executable {
  def execute = {
    val fullCommand = (program)
    fullCommand!!
  }

  def execute(arguments: Seq[String]) = {
    val fullCommand = (program++arguments)
    fullCommand!!
  }
}

trait Client extends Executable{

  val program:Seq[String]
  val commonArguments:Seq[String] 

  override def execute() = {
    execute(commonArguments)
  }

}

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

