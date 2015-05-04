package com.billding.git

import util.matching.Regex

import akka.actor.{ ActorLogging, Props, Actor }

import com.billding.{Client, SubCommand}

import scala.language.postfixOps

import scala.language.implicitConversions

import com.billding.plotting.DataPlottable

object CommitParser {
  def props(repo: Repo): Props = Props(new CommitParser(repo))
}
class CommitParser(repoParam: Repo) extends Actor with ActorLogging with RepoFunctions{
  override def repo: Repo = repoParam

  def receive = {
    case LineDeltas => {
        sender ! DataFile(repo, createDeltas(repo.hashes))
    }
    case FilesChanged => {
        println("lsFiles: " + lsFilesCommand.execute())
        sender ! DataFile(repo, createFileNumberDeltas(repo.hashes))
    }
    case FollowFiles => {
        sender ! DataFile(repo, createFileNumberDeltas(repo.hashes))
    }
  }
}

case class CommitDelta(idx: Long, linesAdded: Long, linesDeleted: Long) extends DataPlottable
{
  def dataString = s"$idx $linesAdded $linesDeleted"
}

case class CommitFileNumberDelta(idx: Long, filesChanged: Long) extends DataPlottable
{
  def dataString = s"$idx $filesChanged"
}
