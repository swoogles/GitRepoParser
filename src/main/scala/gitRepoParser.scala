import scala.sys.process._
import scala.sys.process.Process

import scala.sys.process._
import scala.sys.process.Process
import com.billding.SystemCommands

import argonaut._, Argonaut._
 
//case class Person(name: String, age: Int, things: List[String])
 

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
     

  //implicit def PersonCodecJson =
  //  casecodec3(Person.apply, Person.unapply)("name", "age", "things")

  val home = "/home/bfrasure/"
  val git = Seq("git")
  val jsonLogger = Seq("/home/bfrasure/Repositories/Personal/scripts/gitLogJson.sh")

  val gitRepo = "Repositories/ClashOfClans/"
  //val gitRepo = "NetBeansProjects/smilereminder3/"


  val gitDirectory= home + gitRepo
  val loggerArguments = Seq(gitDirectory)
  //val loggerArguments = Seq("/home/bfrasure/NetBeansProjects/smilereminder3/")
  val gitDirectoryArguments = Seq("--git-dir=", gitDirectory, ".git --work-tree=", gitDirectory)
  val gitShow = "show"
  val gitShowArguments = Seq("--git-dir="+ gitDirectory+ ".git", "--work-tree="+ gitDirectory, gitShow, "411cae9719")

   //git --git-dir=/home/bfrasure/Repositories/ClashOfClans/.git --work-tree=/home/bfrasure/Repositories/ClashOfClans/ show 411cae971973655

  def main(args: Array[String]) = 
  {
    val logOutput = SystemCommands.runFullCommand(jsonLogger, loggerArguments)
    //println(logOutput)
    val workEmail = ""
    val personalEmail = "Bill Frasure <bill.frasure@gmail.com>"

    val dummyOutput = repoInput

    val entries = logOutput.decodeOption[List[LogEntry]].getOrElse(Nil)


    for ( entry <- entries ) {
      println("Author: ", entry.author)
    }
     
    // work with your data types as you normally would
    val niceEntries = entries.map(entry =>
        entry.copy(date = entry.date.orElse(Some("Date"))))

    //val filteredEntries = niceEntries
    val filteredEntries = niceEntries.filter(_.author == personalEmail )
    //val filteredEntries = niceEntries.filter(_.author == workEmail )
     
    // convert back to json, and then to a pretty printed string, alternative
    // ways to print may be nospaces, spaces2, or a custom format
     
    //val json = niceEntries.asJson
    val json = filteredEntries.asJson
    //println(json.spaces4)


    val prettyprinted: String =
      json.spaces4

    val parsed: Option[LogEntry] =
      prettyprinted.decodeOption[LogEntry]

    //val logEntries = input.decodeOption[List[Person]].getOrElse(Nil)

    println(prettyprinted)

    val showOutput = SystemCommands.runFullCommand(git, gitShowArguments )

    //println(showOutput)
    println("LogEntry: ", parsed)
    
  }
}

