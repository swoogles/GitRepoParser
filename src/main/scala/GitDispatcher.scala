package com.billding.git

import com.billding.Plotter
import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props, Actor }

object GitDispatcher {
  def props(filesToWrite: Int): Props = Props(new GitDispatcher(filesToWrite))
}

class GitDispatcher(var filesToWrite: Int) extends Actor with ActorLogging {

  import ammonite.ops.Path

  val tmpBaseDir = Path("/tmp/GitRepoParser")
  val outputDirs = OutputDirectories.initialized(tmpBaseDir)
  val plotter = new Plotter(outputDirs)
  def generateRepoActorId(repo: Repo): String = repo.path.toString.replace("/","_")

  var fileCreator: ActorRef = null

  def receive = {
    case RepoAndAction( repo: Repo, commitAction ) => {
      val repoActorId: String = generateRepoActorId(repo)
      // This part grossly needs to happen first, lest we get a dataFile that can't yet be handled. Figure out how to make this less bad.
      fileCreator = context.actorOf(GitDataFileCreator.props(repo, outputDirs), repoActorId + "fileCreator")

      val commitParser = context.actorOf(CommitParser.props(repo), repoActorId + "commitParser")

      // After parser does its work, it should tell the results to dataFileCreator
      // I'm sure there's a more proper way where dataFileCreator is already the
      // sender, but this will have to do for now.
      commitParser ! commitAction
      println("repo.fileName: " + repo.fileName)
      fileCreator ! plotter.createPlotScript(repo.fileName, commitAction.pp)
    }
    case dataFile: DataFile => {
      fileCreator ! dataFile
    }

    case FileWritten(file) => {
      //context.system.stop(sender) //TODO call this without errors
      println("Finished writing file: " + file)
      filesToWrite -= 1
      println("files left to write: " + filesToWrite)
      val repoName: String = s"actor $sender".split("_")(1).split("#")(0) // Get everything after the first underscore and then before the following #
      if ( filesToWrite == 0 ) {
        println("All Files written. Shutting down.")
        context.system.shutdown()
        plotter.executePlotScripts()
      }
    }
  }
}
