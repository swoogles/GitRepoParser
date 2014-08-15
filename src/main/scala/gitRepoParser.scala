import scala.sys.process._
import scala.sys.process.Process

import scala.sys.process._
import scala.sys.process.Process
import com.billding.SystemCommands

import argonaut._, Argonaut._
 
case class Person(name: String, age: Int, things: List[String])
 

   

object RepoParser {

  implicit def PersonCodecJson =
    casecodec3(Person.apply, Person.unapply)("name", "age", "things")

  val git = Seq("git")
  val jsonLogger = Seq("/home/bfrasure/Repositories/Personal/scripts/gitLogJson.sh")
  val loggerArguments = Seq()

  def main(args: Array[String]) = 
  {
    println(SystemCommands.runFullCommand(jsonLogger, loggerArguments) )
    val person =
      Person("Bam Bam", 2, List("club"))

    val json: Json =
      person.asJson

    println(json)

    val prettyprinted: String =
      json.spaces2

    val parsed: Option[Person] =
      prettyprinted.decodeOption[Person]
  }
}
