
import com.billding.Utility
import com.billding.DataWriter

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props, Actor, Inbox }

case class PlotScript(data:List[String])
case class DataFile(gitRepo: String, data:List[String])

object GitDataFileCreator {
  def props(gitRepo: String): Props = Props(new GitDataFileCreator(gitRepo))
}
class GitDataFileCreator(
  gitRepo: String
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

  val repoFileName: String = gitRepo.replaceAll("/","_").init
  val dataWriter: DataWriter = new DataWriter
  val utility: Utility = new Utility

  def writePlotScript(data:List[String]) = {
    val plotScriptName = "plotfiles/" + repoFileName + ".gnuplot"
    dataWriter.write(data, plotScriptName, utility)
  }

  def writeDataFile(data:List[String]) = {
    println("repoFileName: " + repoFileName)
    val dataFileName = "data/" + repoFileName +".dat" 
    dataWriter.write(data, dataFileName, utility)
  }
}

case class RepoTarget(gitRepo: String, email: String)

case object FileWritten
