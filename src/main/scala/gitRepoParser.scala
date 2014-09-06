import scala.sys.process._
import scala.sys.process.Process

import util.matching.Regex

import scala.sys.process._
import scala.sys.process.Process
import com.billding.SystemCommands

import argonaut._, Argonaut._

case class LogEntry(commit: String, author: String, date: Option[String], message: Option[String])
 
object LogEntry {
  implicit def LogEntryCodecJson: CodecJson[LogEntry] =
    casecodec4(LogEntry.apply, LogEntry.unapply)("commit", "author", "date", "message")
}

class GitWorker(repoDir:String) {

  implicit val program = Seq("git")
  val gitDirectoryArguments = Seq("--git-dir="+ repoDir+ ".git", "--work-tree="+ repoDir)

  def getFirstNum(wordsString:String):Int = {
    val words = wordsString split("\\s+")
    words(0).toInt
  }

  def getFilesChanged(commitInfo:String):Int = {
    val filesChangedPat = "\\d+ files? changed".r

    val filesChangedStrings = filesChangedPat findAllIn commitInfo
    if (filesChangedStrings nonEmpty)
      getFirstNum(filesChangedStrings next) 
    else
      0
  }

  def getLinesAdded(commitInfo:String):Int = {
    val insertionsPat = "\\d+ insertions".r

    val linesAddedStrings = insertionsPat findAllIn commitInfo
    if (linesAddedStrings nonEmpty)
      getFirstNum(linesAddedStrings next) 
    else
      0
  }

  def getLinesDeleted(commitInfo:String):Int = {
    val deletionsPat = "\\d+ deletions".r

    val linesDeletedStrings = deletionsPat  findAllIn commitInfo
    if (linesDeletedStrings nonEmpty)
      getFirstNum(linesDeletedStrings next) 
    else
      0
  }

  //  val multiDigitPat:Regex = "\\d+".r

  //  val filesChangedString = filesChangedPat findAllIn commitInfo next 

  def showFullCommit(hash:String):String = {
    val action = "show"
    val numStat = Seq("--numstat")
    val shortStat = Seq("--shortstat")
    val oneLine = Seq("--oneline")
    SystemCommands.runFullCommand(gitDirectoryArguments++Seq(action, hash)++oneLine++shortStat)
  }
}

class RepoParser 
object RepoParser {

   val repoInput = """
   [{
      "commit": "03d2e959780fe2fcfccb6cd9f08e3685be9781b8",
       "author": "Bill Frasure <bill.frasure@gmail.com>",
        "date": "Fri Aug 15 14:03:11 2014 -0600",
         "message": "Bring-Argonaut-into-project"
   },
   {...}]
   """
     
  val home = "/home/bfrasure/"
  val git = Seq("git")
  val jsonLogger = Seq("/home/bfrasure/Repositories/Personal/scripts/gitLogJson.sh")

  val gitRepo = "Repositories/ClashOfClans/"
  //val gitRepo = "NetBeansProjects/smilereminder3/"


  val repoDir= home + gitRepo
  val loggerArguments = Seq(repoDir)

  def main(args: Array[String]) = 
  {
    val logOutput = SystemCommands.runFullCommand(loggerArguments)(jsonLogger)
    val email = args(0)

    val dummyOutput = repoInput

    val entries = logOutput.decodeOption[List[LogEntry]].getOrElse(Nil)

    val commits = entries.map(x=>x.commit)

    // work with your data types as you normally would
    val niceEntries = entries.map(entry =>
        entry.copy(date = entry.date.orElse(Some("Date"))))

    val filteredEntries = niceEntries.filter(_.author == email )
    val filteredCommits = filteredEntries.map(x=>x.commit)
     
    val json = filteredEntries.asJson

    val prettyprinted: String =
      json.spaces4

    val parsed: Option[LogEntry] =
      prettyprinted.decodeOption[LogEntry]

    val worker = new GitWorker(repoDir)
    val testString = "2 files changed, 40 insertions(+), 37 deletions(-)"
    val numLinesAdded = worker getLinesAdded testString
    println("numLinesAdded: " + numLinesAdded )

    //commits.view.zipWithIndex foreach {case (value,index) => println(value,index)}

    for ( (commit,index) <- filteredCommits.view.zipWithIndex ) {
        println(commit + ": " + index + " " 
          + worker.getFilesChanged( worker.showFullCommit(commit)) + " " 
          + worker.getLinesAdded( worker.showFullCommit(commit)) + " " 
          + worker.getLinesDeleted( worker.showFullCommit(commit)))
    }
    println("Le Fin.")

  }
}

