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
  def receive = {
    case dataFile: DataFile => {
      writeDataFile(dataFile.dataStrings)
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
    val plotScript = outputDirs.plotFiles / (repo.fileName + ".gnuplot")
    dataWriter.write(data, plotScript, utility)
  }

  def writeDataFile(data:List[String]) = {
    val dataFile = outputDirs.dataFiles / repo.fileName +".dat"
    dataWriter.write(data, dataFile, utility)
  }
}

case object FileWritten
