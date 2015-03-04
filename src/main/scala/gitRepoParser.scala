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

class GitWorker(repoDir:String) {

  implicit val program = Seq("git")
  val gitDirectoryArguments = Seq("--git-dir=" + repoDir + ".git", "--work-tree=" + repoDir)

  def getFirstNum(wordsString:String):Int = {
    val words = wordsString split("\\s+")
    words(0).toInt
  }

  def getNumberWithPattern(commitInfo:String, pattern: Regex ):Int = {
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

  def getFilesChanged(commitInfo:String):Int = {
    getNumberWithPattern(commitInfo, "files? changed")
  }

  def getLinesAdded(commitInfo:String):Int = {
    getNumberWithPattern(commitInfo, "insertions")
  }

  def getLinesDeleted(commitInfo:String):Int = {
    getNumberWithPattern(commitInfo, "deletions")
  }

  def showFullCommit(hash:String):String = {
    val action = "show"
    val numStat = Seq("--numstat")
    val shortStat = Seq("--shortstat")
    val oneLine = Seq("--oneline")
    SystemCommands.runFullCommand(gitDirectoryArguments++Seq(action, hash)++shortStat)
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

object RepoParser {

  val home = "/home/bfrasure/"
  val git = Seq("git")
  val jsonLogger = Seq("/home/bfrasure/Repositories/Personal/scripts/gitLogJson.sh")

  def writePlotScript(gitRepo:String, data:List[String]) = {
    val dataWriter: DataWriter = new DataWriter
    val utility: Utility = new Utility

    val plotScriptName = gitRepo.replaceAll("/","_").init
    dataWriter.write(data, "plotfiles/"+plotScriptName+".gnuplot", utility)
  }

  def main(args: Array[String]) = 
  {
    val email = args(0)
    val gitRepo = args(1)

    val repoDir= home + gitRepo
    val loggerArguments = Seq(repoDir)

    val logOutput = SystemCommands.runFullCommand(loggerArguments)(jsonLogger)

    val entries = logOutput.decodeOption[List[LogEntry]].getOrElse(Nil)

    val userEntries = entries.filter(_.author contains email )
    val userCommitHashes = userEntries.map(x=>x.commit)

    val worker = new GitWorker(repoDir)

    val commitDeltas: List[CommitDelta] = userCommitHashes.zipWithIndex map {
      case (hash,idx) => {
        CommitDelta(
          idx, 
          worker.getLinesAdded( worker.showFullCommit(hash)),
          -worker.getLinesDeleted( worker.showFullCommit(hash))
        )
      } 
    }

    val deltaPlots = commitDeltas.map { x => x.toString }

    val dataWriter:DataWriter = new DataWriter
    val utility:Utility = new Utility
    // init->Return all except tail
    val dataFileName = "data/" + gitRepo.replaceAll("/","_").init +".dat" 
    dataWriter.write(deltaPlots, dataFileName, utility)

    val plotter = new GnuPlotter
    val plotScriptName = gitRepo.replaceAll("/","_").init
    val plotScriptData = List(GnuPlotter.createPlotScript(plotter, plotScriptName))

    writePlotScript(gitRepo, plotScriptData)

    println
  }
}

