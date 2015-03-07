import com.billding.GnuPlotter

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props, Actor, Inbox }
import scala.concurrent.duration._

import akka.util.Timeout



object GitDispatcher {
  def props(filesToWrite: Int): Props = Props(new GitDispatcher(filesToWrite))
}
class GitDispatcher(var filesToWrite: Int) extends Actor with ActorLogging {
  val home = "/home/bfrasure/"
  def receive = {
    case DataFile(gitRepo, data) => {
      val repoFileName: String = gitRepo.replaceAll("/","_").init
      val dataForWriting = DataFile(gitRepo, data)
      val dataFileCreator = context.system.actorOf(GitDataFileCreator.props(gitRepo), repoFileName + "dataFileCreator")
      println("Please be hitting this?")
      dataFileCreator ! dataForWriting
    }

    case FileWritten => {
      context.system.stop(sender)
      filesToWrite -= 1
      println("filesToWrite left: " + filesToWrite)
      if ( filesToWrite == 0 ) {
        context.system.shutdown()
        GnuPlotter.executePlotScripts()
      }
    }
    case RepoTarget(gitRepo, email) => {
      println("Let's get to work!")
      val repoFileName: String = gitRepo.replaceAll("/","_").init
      val repoDir= home + gitRepo + "/"
      val jsonLogger = new JsonLogger(repoDir)

      val entries = jsonLogger.repoLogs()

      val userEntries = entries.filter(_.author contains email )

      val userHashes = userEntries.map(x=>GitHash(x.commit))

      val commitParser = context.system.actorOf(CommitParser.props(repoDir), repoFileName + "commitParser")

      implicit val timeout = Timeout(5 seconds)
      //commitParser ! HashList(userHashes)

      val dataFileCreator = context.system.actorOf(GitDataFileCreator.props(gitRepo), repoFileName + "dataFileCreator")

      val plotFileCreator = context.system.actorOf(GitDataFileCreator.props(gitRepo), repoFileName + "plotFileCreator")

      // After parser does its work, it should tell the results to dataFileCreator
      // I'm sure there's a more proper way where dataFileCreator is already the
      // sender, but this will have to do for now.
      commitParser ! HashList(userHashes)

      val plotter = new GnuPlotter
      val plotScriptName = repoFileName
      val plotScriptData = List(plotter.createPlotScript(plotScriptName))

      plotFileCreator ! PlotScript(plotScriptData)
      println("Damn!")
    }
  }
}

object GitManager {
  val home = "/home/bfrasure/"

  def main(args: Array[String]) = 
  {
    val email = args(0)

    val repos = List(
      "AudioHand/Mixer/",
      "ClashOfClans/",
      "GitRepoParser/"
      //"Latex/",
      //"Personal",
      //"Physics"
    )
    val qualifiedRepos = repos.map { "Repositories/" + _ }

    val system = ActorSystem("helloakka")
    val numRepos = repos.size
    val filesPerRepo = 2
    val filesToWrite = numRepos * filesPerRepo
    val dispatcher = system.actorOf(GitDispatcher.props(filesToWrite), "dispatcher")

    for {
      repo <- qualifiedRepos
    } {
      val repoTarget = RepoTarget(repo, email)
      println("About to dispatch")
      dispatcher ! repoTarget
    }

    //Thread.sleep(8000)
    //system.shutdown

    //GnuPlotter.executePlotScripts()
    println
  }
}
