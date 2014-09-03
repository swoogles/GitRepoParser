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

  def getLinesAdded(commitInfo:String):Int = {
    val insertionsPat = "\\d+ insertions".r

    val linesAddedString = insertionsPat findAllIn commitInfo next
    val linesAddedNum = getFirstNum(linesAddedString) 
    linesAddedNum 
  }

  def hasLinesAdded(commitInfo:String):Boolean = {
    val insertionsPat = "\\d+ insertions".r
    insertionsPat findAllIn commitInfo nonEmpty
  }

  //def getNumbers(commitInfo:String) {
  //  val multiDigitPat:Regex = "\\d+".r
  //  val filesChangedPat = "\\d+ files changed".r
  //  val insertionsPat = "\\d+ insertions".r
  //  val deletionsPat = "\\d+ deletions".r

  //  val filesChangedString = filesChangedPat findAllIn commitInfo next 
  //  val filesChangedNum = filesChangedString split("\\s+")(0)
  //  val linesAddedString = insertionsPat findAllIn commitInfo next
  //  val linesAddedNum = linesAddedString(0)

  //  println("linesAddedString: " + linesAddedString)
  //  println("linesAddedNum: " + linesAddedNum)
  //  
  //  //val numbersIterator:scala.util.matching.Regex.MatchIterator  = multiDigitPat findAllIn repoDir

  //}

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
     
    // parse the string as json, attempt to decode it to a list of commits,
    // otherwise just take it as an empty list.

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
     
    // convert back to json, and then to a pretty printed string, alternative
    // ways to print may be nospaces, spaces2, or a custom format
     
    val json = filteredEntries.asJson
    //println(json.spaces4)


    val prettyprinted: String =
      json.spaces4

    val parsed: Option[LogEntry] =
      prettyprinted.decodeOption[LogEntry]

    val worker = new GitWorker(repoDir)
    //worker.showFullCommit("016255b40f")
    println("Bloop: " + worker.getLinesAdded( worker.showFullCommit("411cae971973")) )
    //for ( line <- worker.showFullCommit("411cae971973") ) {
    //  println("Line: " + line)
    //}
    val testString = "2 files changed, 40 insertions(+), 37 deletions(-)"
    //worker getNumbers testString
    val numLinesAdded = worker getLinesAdded testString
    println("numLinesAdded: " + numLinesAdded )

    //commits.view.zipWithIndex foreach {case (value,index) => println(value,index)}

    //commits.view.zipWithIndex foreach {case (value,index) => 
    //  case worker.hasLinesAdded( worker.showFullCommit(commit)) =>
    //    println(commit + ": " + worker.getLinesAdded( worker.showFullCommit(commit)) )
    //println(value,index)}

    for ( (commit,index) <- commits.view.zipWithIndex ) {
      if( worker.hasLinesAdded( worker.showFullCommit(commit)) ) {
        println( index + " " + worker.getLinesAdded( worker.showFullCommit(commit)) )
      }
      else {
        println( index + " " + 0 )
      }
    }
    println("Le Fin.")

  }
}

