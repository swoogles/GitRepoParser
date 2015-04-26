package com.billding.git

import com.billding.Plotter
import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props, Actor }

object GitDispatcher {
  def props(filesToWrite: Int): Props = Props(new GitDispatcher(filesToWrite))
}

class GitDispatcher(var filesToWrite: Int) extends Actor with ActorLogging {

  def receive = {
    case RepoAndAction( repo, commitAction ) => {

      val commitParser = context.actorOf(CommitParser.props(repo), repo.fileName + "commitParser")
      val plotFileCreator = context.actorOf(GitDataFileCreator.props(repo), repo.fileName + "plotFileCreator")

      // After parser does its work, it should tell the results to dataFileCreator
      // I'm sure there's a more proper way where dataFileCreator is already the
      // sender, but this will have to do for now.
      commitParser ! commitAction
      plotFileCreator ! Plotter.createPlotScript(repo.fileName, commitAction.pp)
    }
    case dataFile: DataFile => {
      val dataFileCreator: ActorRef = context.actorOf(GitDataFileCreator.props(dataFile.repo), dataFile.repo.fileName + "dataFileCreator")
      dataFileCreator ! dataFile
    }

    case FileWritten => {
      //context.system.stop(sender) //TODO call this without errors
      filesToWrite -= 1
      val repoName: String = s"actor $sender".split("_")(1).split("#")(0) // Get everything after the first underscore and then before the following #
      if ( filesToWrite == 0 ) {
        context.system.shutdown()
        Plotter.executePlotScripts()
      }
    }
  }
}
