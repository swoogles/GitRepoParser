import scala.sys.process._
import scala.sys.process.Process

import util.matching.Regex

import scala.sys.process._
import scala.sys.process.Process
import com.billding.SystemCommands

import argonaut._, Argonaut._

case class LogEntry(commit: String, author: String, date: Option[String], message: Option[String])

class Utility() {
  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
      val p = new java.io.PrintWriter(f)
        try { op(p) } finally { p.close() }
  }
}
 
object LogEntry {
  implicit def LogEntryCodecJson: CodecJson[LogEntry] =
    casecodec4(LogEntry.apply, LogEntry.unapply)("commit", "author", "date", "message")
}

class DataWriter() {
  def write(data:List[String], outputFile:String, utility:Utility):String = {
    // Write to file
    import java.io._
    utility.printToFile(new File(outputFile))(p => {
        data.foreach(p.println)
    })
    "incomplete"
  }
}

class GnuPlotter() {
  implicit val program = Seq("gnuplot")

  def plot(data:List[String], outputFile:String):String = {
    SystemCommands.runFullCommand(Seq(outputFile))
  }
}

class GitWorker(repoDir:String) {

  implicit val program = Seq("git")
  val gitDirectoryArguments = Seq("--git-dir=" + repoDir + ".git", "--work-tree=" + repoDir)

  def getFirstNum(wordsString:String):Int = {
    val words = wordsString split("\\s+")
    words(0).toInt
  }

  def getNumberWithPattern(commitInfo:String, pattern: Regex ):Int = {
    val matchingStrings = pattern findAllIn commitInfo
    if (matchingStrings nonEmpty)
      getFirstNum(matchingStrings next) 
    else
      0
  }

  def getFilesChanged(commitInfo:String):Int = {
    getNumberWithPattern(commitInfo, "\\d+ files? changed".r)
  }

  def getLinesAdded(commitInfo:String):Int = {
    getNumberWithPattern(commitInfo, "\\d+ insertions".r)
  }

  def getLinesDeleted(commitInfo:String):Int = {
    getNumberWithPattern(commitInfo, "\\d+ deletions".r)
  }

  def showFullCommit(hash:String):String = {
    val action = "show"
    val numStat = Seq("--numstat")
    val shortStat = Seq("--shortstat")
    val oneLine = Seq("--oneline")
    SystemCommands.runFullCommand(gitDirectoryArguments++Seq(action, hash)++shortStat)
  }
}

class RepoParser 
object RepoParser {

  val home = "/home/bfrasure/"
  val git = Seq("git")
  val jsonLogger = Seq("/home/bfrasure/Repositories/Personal/scripts/gitLogJson.sh")

  //val gitRepo = "Repositories/ClashOfClans/"
  val gitRepo = "Repositories/Physics/"
  //val gitRepo = "NetBeansProjects/smilereminder3/"

  val repoDir= home + gitRepo
  val loggerArguments = Seq(repoDir)

  def main(args: Array[String]) = 
  {
    val logOutput = SystemCommands.runFullCommand(loggerArguments)(jsonLogger)
    val email = args(0)

    val entries = logOutput.decodeOption[List[LogEntry]].getOrElse(Nil)

    val commits = entries.map(x=>x.commit)

    val userEntries = entries.filter(_.author contains email )
    val userCommits = userEntries.map(x=>x.commit)
     
    val json = userEntries.asJson

    val prettyprinted: String =
      json.spaces4

    val parsed: Option[LogEntry] =
      prettyprinted.decodeOption[LogEntry]

    val worker = new GitWorker(repoDir)

    //val newCommits = for ( (commit,index) <- userCommits.view.zipWithIndex ) 
    //                  yield commit
    //
    ////newCommits foreach {x=>worker.showFullCommit(x)}
    //newCommits foreach {x=>println(x)}
        

    val data = List("Five","strings","in","a","file!")
    val dataWriter:DataWriter = new DataWriter
    val utility:Utility = new Utility
    // init->Return all except tail
    val dataFile = gitRepo.replaceAll("/","_").init +".dat" 
    dataWriter.write(data, dataFile, utility)
    println("Done")

    for ( (commit,index) <- userCommits.view.zipWithIndex ) {
        println(
          //commit + ": " +
           index + " " +
          //+ worker.getFilesChanged( worker.showFullCommit(commit)) + " " +
          worker.getLinesAdded( worker.showFullCommit(commit)) + " " +
          (-worker.getLinesDeleted( worker.showFullCommit(commit))) 
        )
    }

  }
}

