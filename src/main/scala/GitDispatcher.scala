import com.billding.GnuPlotter
import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props, Actor }

import scala.concurrent.duration._

import akka.util.Timeout

object GitDispatcher {
  def props(filesToWrite: Int): Props = Props(new GitDispatcher(filesToWrite))
}
class GitDispatcher(var filesToWrite: Int) extends Actor with ActorLogging {
  val home = "/home/bfrasure/"
  def receive = {
    case dataFile: DataFile => {
      val repoFileName: String = dataFile.gitRepo.fileName
      val dataFileCreator = context.actorOf(GitDataFileCreator.props(dataFile.gitRepo), repoFileName + "dataFileCreator")

      dataFileCreator ! dataFile
    }

    case FileWritten => {
      //context.system.stop(sender) //TODO call this without errors
      filesToWrite -= 1
      println("filesToWrite left: " + filesToWrite)
      if ( filesToWrite == 0 ) {
        context.system.shutdown()
        GnuPlotter.executePlotScripts()
      }
    }
    case RepoTarget(gitRepo, email) => {
      log.info("Let's get to work!")
      val repoFileName: String = gitRepo.fileName
      val repoDir= home + gitRepo + "/"
      val jsonLogger = new JsonLogger(gitRepo.repoDir)

      val entries = jsonLogger.repoLogs()

      val userEntries = entries.filter(_.author contains email )

      val userHashes = userEntries.map(x=>GitHash(x.commit))

      val commitParser = context.actorOf(CommitParser.props(gitRepo), repoFileName + "commitParser")

      implicit val timeout = Timeout(5 seconds)

      val plotFileCreator = context.actorOf(GitDataFileCreator.props(gitRepo), repoFileName + "plotFileCreator")

      // After parser does its work, it should tell the results to dataFileCreator
      // I'm sure there's a more proper way where dataFileCreator is already the
      // sender, but this will have to do for now.
      commitParser ! HashList(userHashes)

      val plotter = new GnuPlotter
      val plotScriptName = repoFileName
      val plotScriptData = List(plotter.createPlotScript(plotScriptName))

      plotFileCreator ! PlotScript(plotScriptData)
    }
  }
}
