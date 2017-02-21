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
class CommitParser(val repo: Repo) extends Actor with ActorLogging with RepoFunctions{
  def receive = {
    case LineDeltas => {
        println("about to calc lineDeltas and then return to: " + sender.toString())
        sender ! DataFile(repo, createDeltas(repo.hashes))
    }
    case FilesChanged => {
        sender ! DataFile(repo, createFileNumberDeltas(repo.hashes))
    }
    case FollowFiles => {
        sender ! DataFile(repo, createFileNumberDeltas(repo.hashes))
    }
  }
}

case class CommitDelta(idx: Long, linesAdded: Long, linesDeleted: Long) extends DataPlottable
{
  def dataString = s"$idx $linesAdded -$linesDeleted"
}

case class CommitFileNumberDelta(idx: Long, filesChanged: Long) extends DataPlottable
{
  def dataString = s"$idx $filesChanged"
}
