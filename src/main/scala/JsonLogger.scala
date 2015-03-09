package com.billding

import argonaut._
import  Argonaut._

case class LogEntry(commit: String, author: String, date: Option[String], message: Option[String])

object LogEntry {
  implicit def LogEntryCodecJson: CodecJson[LogEntry] =
    casecodec4(LogEntry.apply, LogEntry.unapply)("commit", "author", "date", "message")
}

object JsonLogger {
  implicit val program = Seq("/home/bfrasure/Repositories/Personal/scripts/gitLogJson.sh")

  def repoLogs(repoDir:String) = {
    val loggerArguments = Seq(repoDir)
    val logOutput = SystemCommands.runFullCommand(loggerArguments)
    val entries: List[LogEntry] = logOutput.decodeOption[List[LogEntry]].getOrElse(Nil)
    entries
  }
}

