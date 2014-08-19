import scala.sys.process._
import scala.sys.process.Process

import scala.sys.process._
import scala.sys.process.Process
import com.billding.SystemCommands

import argonaut._, Argonaut._
 
case class Address(street: String, number: Int, postcode: Int)
 
object Address {
  // Define codecs easily from case classes
  implicit def AddressCodecJson: CodecJson[Address] =
    casecodec3(Address.apply, Address.unapply)("street", "number", "post_code")
}
 
case class Person(name: String, age: Int, address: Option[Address], greeting: Option[String])
 
object Person {
  implicit def PersonCodecJson: CodecJson[Person] =
    casecodec4(Person.apply, Person.unapply)("name", "age", "address", "greeting")
}

case class LogEntry(commit: String, author: String, date: Option[String], message: Option[String])
 
object LogEntry {
  implicit def LogEntryCodecJson: CodecJson[LogEntry] =
    casecodec4(LogEntry.apply, LogEntry.unapply)("commit", "author", "date", "message")
}

class GitWorker(repoDir:String) {
  val program = Seq("git")
  val gitDirectoryArguments = Seq("--git-dir="+ repoDir+ ".git", "--work-tree="+ repoDir)
  val gitShow = "show"
  val gitShowArguments = gitDirectoryArguments ++ Seq(gitShow, "411cae9719")


  def showFullCommit(hash:String):String = {
    SystemCommands.runFullCommand(program, gitShowArguments )
  }
}

   

object RepoParser {

   val repoInput = """
   [{
      "commit": "03d2e959780fe2fcfccb6cd9f08e3685be9781b8",
       "author": "Bill Frasure <bill.frasure@gmail.com>",
        "date": "Fri Aug 15 14:03:11 2014 -0600",
         "message": "Bring-Argonaut-into-project"
   },
   {
      "commit": "5190e305780844743134943be768076e0721e890",
       "author": "Bill Frasure <bill.frasure@gmail.com>",
        "date": "Fri Aug 15 13:57:26 2014 -0600",
         "message": "Add-some-filetypes-ignores-for-Scala"
   }]
   """
     
    // parse the string as json, attempt to decode it to a list of person,
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
    val logOutput = SystemCommands.runFullCommand(jsonLogger, loggerArguments)
    val email = args(0)

    val dummyOutput = repoInput

    val entries = logOutput.decodeOption[List[LogEntry]].getOrElse(Nil)


    val commits = entries.map(x=>x.commit)
    //commits.foreach(println)

    // work with your data types as you normally would
    val niceEntries = entries.map(entry =>
        entry.copy(date = entry.date.orElse(Some("Date"))))

    val filteredEntries = niceEntries.filter(_.author == email )
     
    // convert back to json, and then to a pretty printed string, alternative
    // ways to print may be nospaces, spaces2, or a custom format
     
    val json = filteredEntries.asJson
    println(json.spaces4)


    val prettyprinted: String =
      json.spaces4

    val parsed: Option[LogEntry] =
      prettyprinted.decodeOption[LogEntry]

    val worker = new GitWorker(repoDir)
    //println(worker.showFullCommit("411cae971973"))
  }
}

