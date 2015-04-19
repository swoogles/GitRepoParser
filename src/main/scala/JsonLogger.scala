package com.billding

//import argonaut.{CodecJson}
//import  Argonaut.EncodeJson

import argonaut.{
  Argonaut,
  CodecJson
}
import  Argonaut._

import com.billding.git.Repo

import akka.actor.{ ActorLogging, Props, Actor }

case class LogEntry(commit: String, author: String, date: Option[String], message: Option[String])

case class RepoLogs(repo: Repo, logEntries: List[LogEntry])

object LogEntry {
  implicit def LogEntryCodecJson: CodecJson[LogEntry] =
    casecodec4(LogEntry.apply, LogEntry.unapply)("commit", "author", "date", "message")
}

class JsonLogger extends Actor with ActorLogging {
  def receive = {
    case repo: Repo => {
      sender ! JsonLogger.repoLogs(repo)
    }
  }
}

object JsonLogger {
  implicit val program = Seq("/home/bfrasure/Repositories/Personal/scripts/gitLogJson.sh")

  def props(): Props = Props(new JsonLogger())

  def repoLogs(repo: Repo): RepoLogs  = {
    val loggerArguments = Seq(repo.dir)
    val logOutput = SystemCommands.runFullCommand(loggerArguments)
    val entries: List[LogEntry] = logOutput.decodeOption[List[LogEntry]].getOrElse(Nil)
    RepoLogs(repo, entries)
  }
}

