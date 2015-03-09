package com.billding.git

import com.billding.GnuPlotter
import com.billding.JsonLogger
import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props, Actor }

object GitDispatcher {
  def props(filesToWrite: Int): Props = Props(new GitDispatcher(filesToWrite))
}
class GitDispatcher(var filesToWrite: Int) extends Actor with ActorLogging {
  def receive = {
    case dataFile: DataFile => {
      val dataFileCreator = context.actorOf(GitDataFileCreator.props(dataFile.gitRepo), dataFile.gitRepo.fileName + "dataFileCreator")

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
      val entries = JsonLogger.repoLogs(gitRepo.dir)

      val userEntries = entries.filter(_.author contains email )

      val userHashes = userEntries.map(x=>GitHash(x.commit))

      val commitParser = context.actorOf(CommitParser.props(gitRepo), gitRepo.fileName + "commitParser")

      val plotFileCreator = context.actorOf(GitDataFileCreator.props(gitRepo), gitRepo.fileName + "plotFileCreator")

      // After parser does its work, it should tell the results to dataFileCreator
      // I'm sure there's a more proper way where dataFileCreator is already the
      // sender, but this will have to do for now.
      commitParser ! HashList(userHashes)

      val plotter = new GnuPlotter

      plotFileCreator ! plotter.createPlotScript(gitRepo.fileName)
    }
  }
}
