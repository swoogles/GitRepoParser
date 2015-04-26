package com.billding.git

import util.matching.Regex

import akka.actor.{ ActorLogging, Props, Actor }

import com.billding.{Client, SubCommand}

import scala.language.postfixOps

import scala.language.implicitConversions

sealed trait CommitAction
object LineDeltas extends CommitAction
object FilesChanged extends CommitAction

object CommitParser {
  def props(repo: Repo): Props = Props(new CommitParser(repo))
}
class CommitParser(repo: Repo) extends Actor with ActorLogging with Client{

  def receive = {
    case LineDeltas => {
        sender ! DataFile(repo, createDeltas(repo.hashes))
    }
    case FilesChanged => {
        sender ! DataFile(repo, createDeltas(repo.hashes))
    }
  }

  val program = Seq("git")
  val persistentArguments = Seq("--git-dir=" + repo.dir + ".git", "--work-tree=" + repo.dir)

  def getFirstNum(wordsString:String):Int = {
    val words = wordsString split("\\s+")
    words(0).toInt
  }

  def getNumberWithPattern(hash: GitHash, pattern: Regex ):Int = {
    val commitInfo = logFullCommit(hash)
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

  def getFilesChanged(hash: GitHash):Int = getNumberWithPattern(hash, "files? changed")
  def getLinesAdded(hash: GitHash):Int = getNumberWithPattern(hash, "insertions")
  def getLinesDeleted(hash: GitHash):Int = getNumberWithPattern(hash, "deletions")

  def logFullCommit(gitHash: GitHash):String = {
    sealed abstract class DisplayVariant {
      val parameter: Seq[String]
    }
    case object NUMSTAT extends DisplayVariant { val parameter = Seq("--numstat") }
    case object SHORTSTAT extends DisplayVariant { val parameter = Seq("--shortstat") }
    case object ONELINE extends DisplayVariant { val parameter = Seq("--oneline") }

    repo.logCommand.execute(Seq(gitHash.hash) ++ SHORTSTAT.parameter)
  }

  def createDeltas(hashes: List[GitHash]): List[CommitDelta] = {
    val commitDeltas: List[CommitDelta] = hashes.zipWithIndex map {
      case (hash,idx) => {
        CommitDelta(
          idx, 
          getLinesAdded(hash),
          -getLinesDeleted(hash)
        )
      } 
    }
    commitDeltas
  }
}

case class CommitDelta(idx: Long, linesAdded: Long, linesDeleted: Long)
{
  override def toString(): String = s"$idx $linesAdded $linesDeleted"
}
object CommitDelta {
  implicit def stringifier(cd: CommitDelta): String = cd.toString
  implicit def multiStringifier(cds: List[CommitDelta]): List[String] = cds map { cd => cd.toString }
}

