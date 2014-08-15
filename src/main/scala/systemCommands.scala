package com.billding

import scala.sys.process._
import scala.sys.process.Process
import scala.sys.process.ProcessBuilder

object SystemCommands {
  def runFullCommand( command:Seq[String], arguments:Seq[String] ):String = {
    val fullCommand = (command++arguments)
    //println("Full Command: " + fullCommand )
    //Process(fullCommand).lineStream.last
    fullCommand.run
    //ProcessBuilder(command++arguments).lineStream
    //val output = fullCommand.lineStream.foreach(println)
    "returned"
  }
}

