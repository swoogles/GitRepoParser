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

import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Inbox }
import scala.concurrent.duration._

case object Greet
case class WhoToGreet(who: String)
case class Greeting(message: String)

class Greeter extends Actor {
  var greeting = ""

  def receive = {
    case WhoToGreet(who) => greeting = s"hello, $who"
    case Greet           => sender ! Greeting(greeting)
  }
}

    case class RepoToExamine(url: String)

    class RepoExaminer extends Actor {
      var repo = ""

      def receive = {
        case RepoToExamine(url) => sender ! RepoData(3, "projectNameA")
        case Greet           => sender ! RepoData(3, "projectNameA")
      }
    }

        case class RepoData(numCommits: Int, projectName: String)

        class ParserActor extends Actor {
          def receive = {
            case RepoData(numCommits, projectName) => println("numCommits: " + numCommits)
          }
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

          def actorTest(): Unit = {
            val system = ActorSystem("helloakka")
            val greeter = system.actorOf(Props[Greeter], "greeter")

            val targetRepo = RepoToExamine("clashOfClans")
            val parser = system.actorOf(Props[ParserActor], "parser")
            val examiner = system.actorOf(Props[RepoExaminer], "examiner")

            examiner.tell(targetRepo, parser)


            greeter.tell(WhoToGreet("akka"), ActorRef.noSender)
            //greeter ! WhoToGreet("akka")

            // Create an "actor-in-a-box"
            val inbox = Inbox.create(system)

            // Tell the 'greeter' to change its 'greeting' message
            greeter.tell(WhoToGreet("akka2"), ActorRef.noSender)

            // Ask the 'greeter for the latest 'greeting'
            // Reply should go to the mailbox
            inbox.send(greeter, Greet)

            // Wait 5 seconds for the reply with the 'greeting' message
            val Greeting(message) = inbox.receive(5.seconds)
            println(s"Greeting: $message")

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

            val entries = logOutput.decodeOption[List[LogEntry]].getOrElse(Nil)

            val userEntries = entries.filter(_.author contains email )
            val userHashes = userEntries.map(x=>GitHash(x.commit))

            val worker = new GitWorker(repoDir)

            val commitDeltas: List[CommitDelta] = userHashes.zipWithIndex map {
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

