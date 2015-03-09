package com.billding

import argonaut._
import  Argonaut._

import com.billding.git.GitRepo

import akka.actor.{ ActorLogging, Props, Actor }

case class LogEntry(commit: String, author: String, date: Option[String], message: Option[String])

case class RepoLogs(gitRepo: GitRepo, logEntries: List[LogEntry])

object LogEntry {
  implicit def LogEntryCodecJson: CodecJson[LogEntry] =
    casecodec4(LogEntry.apply, LogEntry.unapply)("commit", "author", "date", "message")
}

class JsonLogger extends Actor with ActorLogging {
  def receive = {
    case gitRepo: GitRepo => {
      sender ! JsonLogger.repoLogs(gitRepo)
    }
  }
}

object JsonLogger {
  implicit val program = Seq("/home/bfrasure/Repositories/Personal/scripts/gitLogJson.sh")

  def props(): Props = Props(new JsonLogger())

  def repoLogs(gitRepo: GitRepo): RepoLogs  = {
    val loggerArguments = Seq(gitRepo.dir)
    val logOutput = SystemCommands.runFullCommand(loggerArguments)
    val entries: List[LogEntry] = logOutput.decodeOption[List[LogEntry]].getOrElse(Nil)
    RepoLogs(gitRepo, entries)
  }
}

