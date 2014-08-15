package com.billding

import scala.sys.process._
import scala.sys.process.Process
import scala.sys.process.ProcessBuilder

object SystemCommands {
  def runFullCommand( command:Seq[String], arguments:Seq[String] ):String = {
    val fullCommand = (command++arguments)
    //Process(fullCommand).lineStream.last
    //ProcessBuilder(fullCommand).lineStream.toString
    fullCommand!!

    //fullCommand.run //Best version
    //val output = fullCommand.lineStream.foreach(println)
    //"returned"
  }
}

