
import com.billding.Utility
import com.billding.DataWriter

import akka.actor.{ ActorLogging, Props, Actor }

case class PlotScript(data:List[String])
case class DataFile(gitRepo: GitRepo, data:List[String])

object GitDataFileCreator {
  def props(gitRepo: GitRepo): Props = Props(new GitDataFileCreator(gitRepo))
}
class GitDataFileCreator(
  gitRepo: GitRepo
) extends Actor with ActorLogging
{
  def receive = {
    case DataFile(gitRepo, data) => {
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
    val plotScriptName = "plotfiles/" + gitRepo.fileName + ".gnuplot"
    dataWriter.write(data, plotScriptName, utility)
  }

  def writeDataFile(data:List[String]) = {
    val dataFileName = "data/" + gitRepo.fileName +".dat" 
    dataWriter.write(data, dataFileName, utility)
  }
}

case object FileWritten
