import argonaut._
import  Argonaut._
import com.billding.SystemCommands

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

