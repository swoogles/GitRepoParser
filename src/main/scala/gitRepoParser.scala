import scala.sys.process._
import scala.sys.process.Process

import util.matching.Regex

import scala.sys.process._
import scala.sys.process.Process
import com.billding.SystemCommands
import com.billding.GnuPlotter
import com.billding.Utility
import com.billding.DataWriter

import argonaut._, Argonaut._

case class LogEntry(commit: String, author: String, date: Option[String], message: Option[String])

object LogEntry {
  implicit def LogEntryCodecJson: CodecJson[LogEntry] =
    casecodec4(LogEntry.apply, LogEntry.unapply)("commit", "author", "date", "message")
}

case class GitHash( hash: String)

class GitWorker(repoDir:String) {

  implicit val program = Seq("git")
  val gitDirectoryArguments = Seq("--git-dir=" + repoDir + ".git", "--work-tree=" + repoDir)

  def getFirstNum(wordsString:String):Int = {
    val words = wordsString split("\\s+")
    words(0).toInt
  }

  def getNumberWithPattern(hash: GitHash, pattern: Regex ):Int = {
    val commitInfo = showFullCommit(hash)
    val combinedRegex = ("\\d+ " + pattern.toString).r
    val matchingStrings = combinedRegex findAllIn commitInfo
    if (matchingStrings nonEmpty)
      getFirstNum(matchingStrings next) 
    else
      0
  }

  implicit def stringToRegex(patternString: String): Regex = {
    patternString.r
  }

  def getFilesChanged(hash: GitHash):Int = {
    getNumberWithPattern(hash, "files? changed")
  }

  def getLinesAdded(hash: GitHash):Int = {
    getNumberWithPattern(hash, "insertions")
  }

  def getLinesDeleted(hash: GitHash):Int = {
    getNumberWithPattern(hash, "deletions")
  }

  def showFullCommit(gitHash: GitHash):String = {
    val action = "show"
    val numStat = Seq("--numstat")
    val shortStat = Seq("--shortstat")
    val oneLine = Seq("--oneline")
    SystemCommands.runFullCommand(gitDirectoryArguments++Seq(action, gitHash.hash)++shortStat)
  }
}

case class CommitDelta(idx: Long, linesAdded: Long, linesDeleted: Long)
{
  override def toString(): String = {
    idx + " " + 
    linesAdded + " " +
    linesDeleted
  }
}
object CommitDelta {
  implicit def stringifier(cd: CommitDelta): String = cd.toString
}

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props, Actor, Inbox }
import scala.concurrent.duration._

case class RepoToExamine(url: String)

class RepoExaminer extends Actor with ActorLogging{
  val home = "/home/bfrasure/"
  val git = Seq("git")
  val jsonLogger = Seq("/home/bfrasure/Repositories/Personal/scripts/gitLogJson.sh")

  var repo = ""

  def receive = {
    case RepoToExamine(url) => {
      sender ! retrieveRepoData(url)
    }
    //case Greet           => sender ! RepoData(3, "projectNameA")
  }

  def retrieveRepoData(gitRepo: String): RepoData = {

    val repoDir= home + gitRepo
    val loggerArguments = Seq(repoDir)

    val logOutput = SystemCommands.runFullCommand(loggerArguments)(jsonLogger)
    val entries: List[LogEntry] = logOutput.decodeOption[List[LogEntry]].getOrElse(Nil)
    RepoData(entries)
  }
}

case class RepoData(entries: List[LogEntry])

class ParserActor extends Actor with ActorLogging{
  def receive = {
    case RepoData(entries) => println("first entry: " + entries.head)
    case unknown => {
      log.info("Unknown result: " + unknown)
    }
  }
}


object GitDataFileCreator {
  val dataWriter: DataWriter = new DataWriter
  val utility: Utility = new Utility

  def writePlotScript(gitRepo:String, data:List[String]) = {
    val plotScriptName = gitRepo.replaceAll("/","_").init
    dataWriter.write(data, "plotfiles/"+plotScriptName+".gnuplot", utility)
  }

  def writeDataFile(gitRepo:String, data:List[String]) = {
    val dataFileName = "data/" + gitRepo.replaceAll("/","_").init +".dat" 
    dataWriter.write(data, dataFileName, utility)
  }
}


object GitManager {

  val home = "/home/bfrasure/"
  val git = Seq("git")
  val jsonLogger = Seq("/home/bfrasure/Repositories/Personal/scripts/gitLogJson.sh")


  def actorTest(): Unit = {
    val system = ActorSystem("helloakka")

    val targetRepo = RepoToExamine("Repositories/ClashOfClans/")
    val parser = system.actorOf(Props[ParserActor], "parser")
    val examiner = system.actorOf(Props[RepoExaminer], "examiner")

    examiner.tell(targetRepo, parser)

    //system.stop
    //system.awaitTermination()
    Thread.sleep(1000)
    system.shutdown
    println
  }

  def main(args: Array[String]) = 
  {
    actorTest()
    val email = args(0)
    val gitRepo = args(1)

    val repoDir= home + gitRepo
    val loggerArguments = Seq(repoDir)

    val logOutput = SystemCommands.runFullCommand(loggerArguments)(jsonLogger)

    //println("logOutput: " + logOutput)

    val entries: List[LogEntry] = logOutput.decodeOption[List[LogEntry]].getOrElse(Nil)

    val userEntries = entries.filter(_.author contains email )
    val userHashes = userEntries.map(x=>GitHash(x.commit))

    val worker = new GitWorker(repoDir)

    val commitDeltas: List[CommitDelta] = userHashes.zipWithIndex map {
      case (hash,idx) => {
        CommitDelta(
          idx, 
          worker.getLinesAdded(hash),
          -worker.getLinesDeleted(hash)
        )
      } 
    }

    val deltaPlots = commitDeltas.map { x => x.toString }

    GitDataFileCreator.writeDataFile(gitRepo, deltaPlots)

    val plotter = new GnuPlotter
    val plotScriptName = gitRepo.replaceAll("/","_").init
    val plotScriptData = List(GnuPlotter.createPlotScript(plotter, plotScriptName))

    GitDataFileCreator.writePlotScript(gitRepo, plotScriptData)

    println
  }
}

