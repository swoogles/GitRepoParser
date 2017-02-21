package com.billding.git

import com.billding.Utility
import com.billding.DataWriter
import com.billding.PlotScript
import akka.actor.{Actor, ActorLogging, Props}
import ammonite.ops.Path
import com.billding.plotting.DataPlottable
import ammonite.ops.mkdir

case class DataFile(repo: Repo, data:List[DataPlottable]) {
  val dataStrings = data map { datum => datum.dataString }
}

case class OutputDirectories(baseDir: Path) {
  val plotFiles = baseDir / "plotfiles"
  val dataFiles = baseDir / "data"
  val images = baseDir / "images"
}

object OutputDirectories {
  def initialized(baseDir: Path) = {
    val dirs = OutputDirectories(baseDir)
    initialize(dirs)
    dirs
  }

  private def initialize(x: OutputDirectories) = {
    mkdir! x.plotFiles
    mkdir! x.dataFiles
    mkdir! x.images
  }
}

object GitDataFileCreator {
  def props(repo: Repo, outputDirs: OutputDirectories): Props = Props(new GitDataFileCreator(repo, outputDirs))
}
class GitDataFileCreator(
  repo: Repo,
  val outputDirs: OutputDirectories
) extends Actor with ActorLogging
{
  val plotScript = outputDirs.plotFiles / (repo.fileName + ".gnuplot")
  val dataFile = outputDirs.dataFiles / (repo.fileName +".dat")

  def receive = {
    case dataFile: DataFile => {
      println("received data file. Time to write for repo: " + repo)
      writeDataFile(dataFile.dataStrings)
      sender ! FileWritten(plotScript)
    }
    case PlotScript(data) => {
      println("received plotscript file. Time to write for repo: " + repo)
      writePlotScript(data)
      sender ! FileWritten(dataFile)
    }
  }

  val dataWriter: DataWriter = new DataWriter
  val utility: Utility = new Utility

  def writePlotScript(data:List[String]) = {
    dataWriter.write(data, plotScript, utility)
  }

  def writeDataFile(data:List[String]) = {
    dataWriter.write(data, dataFile, utility)
  }
}

case class FileWritten(file: Path)
