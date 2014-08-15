import scala.sys.process._
import scala.sys.process.Process

import scala.sys.process._
import scala.sys.process.Process
import com.billding.SystemCommands

object RepoParser {
  val git = Seq("git")
  val jsonLogger = Seq("/home/bfrasure/Repositories/Personal/scripts/gitLogJson.sh")
  val loggerArguments = Seq()

  def main(args: Array[String]) = 
  {
    println(SystemCommands.runFullCommand(jsonLogger, loggerArguments) )
  }
}
