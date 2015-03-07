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

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props, Actor, Inbox }
import scala.concurrent.duration._

import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask

case class LogEntry(commit: String, author: String, date: Option[String], message: Option[String])

object LogEntry {
  implicit def LogEntryCodecJson: CodecJson[LogEntry] =
    casecodec4(LogEntry.apply, LogEntry.unapply)("commit", "author", "date", "message")
}

class JsonLogger(repoDir:String) {
  implicit val program = Seq("/home/bfrasure/Repositories/Personal/scripts/gitLogJson.sh")

  def repoLogs() = {
    val loggerArguments = Seq(repoDir)
    val logOutput = SystemCommands.runFullCommand(loggerArguments)
    val entries: List[LogEntry] = logOutput.decodeOption[List[LogEntry]].getOrElse(Nil)
    entries
  }
}

case class GitHash( hash: String)
case class HashList( hashes: List[GitHash] )

object CommitParser {
  def props(repoDir: String): Props = Props(new CommitParser(repoDir))
}
class CommitParser(repoDir:String) extends Actor with ActorLogging{

  def receive = {
    case HashList(hashes) => {
      sender ! createDeltas(hashes)
    }
    //case Greet           => sender ! RepoData(3, "projectNameA")
  }


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

  def getFilesChanged(hash: GitHash):Int = getNumberWithPattern(hash, "files? changed")
  def getLinesAdded(hash: GitHash):Int = getNumberWithPattern(hash, "insertions")
  def getLinesDeleted(hash: GitHash):Int = getNumberWithPattern(hash, "deletions")

  def showFullCommit(gitHash: GitHash):String = {
    val action = "show"
    val numStat = Seq("--numstat")
    val shortStat = Seq("--shortstat")
    val oneLine = Seq("--oneline")
    SystemCommands.runFullCommand(gitDirectoryArguments++Seq(action, gitHash.hash)++shortStat)
  }

  def createDeltas(hashes: List[GitHash]): List[CommitDelta] = {
    val commitDeltas: List[CommitDelta] = hashes.zipWithIndex map {
      case (hash,idx) => {
        CommitDelta(
          idx, 
          getLinesAdded(hash),
          -getLinesDeleted(hash)
        )
      } 
    }
    commitDeltas
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
  implicit def multiStringifier(cds: List[CommitDelta]): List[String] = cds map { cd => cd.toString }
}


case class RepoToExamine(url: String)

class RepoExaminer extends Actor with ActorLogging{
  val home = "/home/bfrasure/"
  val git = Seq("git")
  val jsonLogger = Seq("/home/bfrasure/Repositories/Personal/scripts/gitLogJson.sh")

  def receive = {
    case RepoToExamine(url) => {
      sender ! retrieveRepoData(url)
    }
    //case Greet           => sender ! RepoData(3, "projectNameA")
  }

  def retrieveRepoData(gitRepo: String): RepoData = {
    val repoDir= home + gitRepo

    val jsonLogger = new JsonLogger(repoDir)
    val entries = jsonLogger.repoLogs()
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

case class PlotScriptData(data:List[String])
case class DataFileData(data:List[String])

object GitDataFileCreator {
  def props(gitRepo: String): Props = Props(new GitDataFileCreator(gitRepo))
}
class GitDataFileCreator(
  gitRepo: String
) extends Actor with ActorLogging
{
  def receive = {
    case RepoData(entries) => println("first entry: " + entries.head)
    case PlotScriptData(data) => writePlotScript(data)
    case DataFileData(data) => writeDataFile(data)
  }

  val repoFileName: String = gitRepo.replaceAll("/","_").init
  val dataWriter: DataWriter = new DataWriter
  val utility: Utility = new Utility

  def writePlotScript(data:List[String]) = {
    val plotScriptName = "plotfiles/" + repoFileName + ".gnuplot"
    dataWriter.write(data, plotScriptName, utility)
  }

  def writeDataFile(data:List[String]) = {
    val dataFileName = "data/" + repoFileName +".dat" 
    dataWriter.write(data, dataFileName, utility)
  }
}

case class RepoTarget(gitRepo: String, email: String)


object GitDispatcher {
  def props(system: ActorSystem): Props = Props(new GitDispatcher(system))
}
class GitDispatcher(system: ActorSystem) extends Actor with ActorLogging {
  val home = "/home/bfrasure/"
  def receive = {
    case RepoTarget(gitRepo, email) => {
      val repoFileName: String = gitRepo.replaceAll("/","_").init
      val repoDir= home + gitRepo + "/"
      val jsonLogger = new JsonLogger(repoDir)

      val entries = jsonLogger.repoLogs()

      val userEntries = entries.filter(_.author contains email )

      val userHashes = userEntries.map(x=>GitHash(x.commit))

      val commitParser = system.actorOf(CommitParser.props(repoDir), repoFileName + "commitParser")

      implicit val timeout = Timeout(5 seconds)
      //commitParser ! HashList(userHashes)
      val future = commitParser ? HashList(userHashes)
      val commitDeltas: List[CommitDelta] = Await.result(future, timeout.duration).asInstanceOf[List[CommitDelta]]

      val dataFileCreator = system.actorOf(GitDataFileCreator.props(gitRepo), repoFileName + "dataFileCreator")

      dataFileCreator ! DataFileData(commitDeltas)

      val plotter = new GnuPlotter
      val plotScriptName = repoFileName
      val plotScriptData = List(plotter.createPlotScript(plotScriptName))

      dataFileCreator ! PlotScriptData(plotScriptData)
      println("Damn!")
    }
  }
}

object GitManager {
  val home = "/home/bfrasure/"

  def main(args: Array[String]) = 
  {
    val email = args(0)
    val gitRepo = args(1)
    val repoDir= home + gitRepo + "/"

    val repos = List(
      "AudioHand/Mixer",
      "ClashOfClans/",
      "GitRepoParser/",
      "Latex/"
    )
    val qualifiedRepos = repos.map { "Repositories/" + _ }

    val system = ActorSystem("helloakka")
    val dispatcher = system.actorOf(GitDispatcher.props(system), "dispatcher")
    val repoTargetA = RepoTarget(gitRepo, email)
    dispatcher ! repoTargetA

    val repoTargetB = RepoTarget("Repositories/Personal", email)
    dispatcher ! repoTargetB

    for {
      repo <- qualifiedRepos
    } {
      val repoTarget = RepoTarget(repo, email)
      dispatcher ! repoTarget
    }

    Thread.sleep(8000)
    system.shutdown

    GnuPlotter.executePlotScripts()
    println
  }
}
