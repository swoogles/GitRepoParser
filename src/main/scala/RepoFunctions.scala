package com.billding.git

import com.billding.{Client, SubCommand}

import util.matching.Regex

import scala.language.postfixOps
import scala.language.implicitConversions

import com.billding.plotting.DataPlottable

trait RepoFunctions extends Client{ self =>
  def repo: Repo
  val program = Seq("git")
  def persistentArguments = Seq("--git-dir=" + repo.path.toString + "/.git", "--work-tree=" + repo.path.toString)

  val showCommand = SubCommand(program, persistentArguments,"show")
  val todayCommand = SubCommand(program, persistentArguments,"today")
  val branchCommand = SubCommand(program, persistentArguments,"branch")
  val statusCommand = SubCommand(program, persistentArguments,"status")
  def status = statusCommand.execute()

  val lsFilesCommand = SubCommand(program, persistentArguments,"ls-files")

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

  def createDeltas(hashes: List[GitHash]): List[DataPlottable] = {
    val commitDeltas: List[CommitDelta] = hashes.zipWithIndex map {
      case (hash,idx) => CommitDelta( idx, getLinesAdded(hash), -getLinesDeleted(hash))
    }
    commitDeltas
  }

  def createFileNumberDeltas(hashes: List[GitHash]): List[DataPlottable] = {
    val commitDeltas: List[CommitFileNumberDelta] = hashes.zipWithIndex map {
      case (hash,idx) => CommitFileNumberDelta( idx, getFilesChanged(hash)) 
    }
    commitDeltas
  }
}
