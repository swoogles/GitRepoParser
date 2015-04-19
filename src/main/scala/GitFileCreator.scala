package com.billding.git

import com.billding.Utility
import com.billding.DataWriter
import com.billding.PlotScript

import akka.actor.{ ActorLogging, Props, Actor }

case class DataFile(repo: Repo, data:List[String])

object GitDataFileCreator {
  def props(repo: Repo): Props = Props(new GitDataFileCreator(repo))
}
class GitDataFileCreator(
  repo: Repo
) extends Actor with ActorLogging
{
  def receive = {
    case DataFile(repo, data) => {
      writeDataFile(data)
      sender ! FileWritten
    }
    case PlotScript(data) => {
      writePlotScript(data)
      sender ! FileWritten
    }
  }

  val dataWriter: DataWriter = new DataWriter
  val utility: Utility = new Utility

  def writePlotScript(data:List[String]) = {
    val plotScriptName = "plotfiles/" + repo.fileName + ".gnuplot"
    dataWriter.write(data, plotScriptName, utility)
  }

  def writeDataFile(data:List[String]) = {
    val dataFileName = "data/" + repo.fileName +".dat" 
    dataWriter.write(data, dataFileName, utility)
  }
}

case object FileWritten
