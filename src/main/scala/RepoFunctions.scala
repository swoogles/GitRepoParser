package com.billding.git

import com.billding.{Client, SubCommand}

import util.matching.Regex

import scala.language.postfixOps
import scala.language.implicitConversions

import com.billding.plotting.DataPlottable

trait RepoFunctions extends Client{ self =>
  val repo: Repo
  val program = Seq("git")
  val persistentArguments = Seq(s"--git-dir=${repo.path.toString}/.git", s"--work-tree=${repo.path.toString}")

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

  case class CommitMetaData(hash: GitHash) {
    val shortStatInfo = logFullCommit(hash)

    def getNumberWithPatternFromShortStat(commitInfo: String, pattern: Regex ):Int = {
      val combinedRegex = ("\\d+ " + pattern.toString).r
      val matchingStrings = combinedRegex findAllIn commitInfo
      if (matchingStrings nonEmpty)
        getFirstNum(matchingStrings next)
      else
        0
    }


    val filesChanged:Int = getNumberWithPatternFromShortStat(shortStatInfo, "files? changed")
    val linesAdded:Int = getNumberWithPatternFromShortStat(shortStatInfo, "insertions")
    val linesDeleted:Int = getNumberWithPatternFromShortStat(shortStatInfo, "deletions")
  }

  def logFullCommit(gitHash: GitHash):String = {
    sealed abstract class DisplayVariant {
      val parameter: Seq[String]
    }
    case object NUMSTAT extends DisplayVariant { val parameter = Seq("--numstat") }
    case object SHORTSTAT extends DisplayVariant { val parameter = Seq("--shortstat") }
    case object ONELINE extends DisplayVariant { val parameter = Seq("--oneline") }

    repo.showCommand.execute(Seq(gitHash.hash) ++ SHORTSTAT.parameter)
  }

  // TODO Approach this with "git log" on the head, rather than potentially thousands of external calls to "git show"
  def createDeltas(hashes: List[GitHash]): List[DataPlottable] = {
    val commitDeltas: List[CommitDelta] = hashes.zipWithIndex map {
      case (hash,idx) =>
        // I think some of my terrible performance is due to multiple external commands here.
        val metaData = CommitMetaData(hash)
        CommitDelta( idx, metaData.linesAdded, metaData.linesDeleted)
    }
    commitDeltas
  }

  def createFileNumberDeltas(hashes: List[GitHash]): List[DataPlottable] = {
    val commitDeltas: List[CommitFileNumberDelta] = hashes.zipWithIndex map {
      case (hash,idx) =>
        val metaData = CommitMetaData(hash)
        CommitFileNumberDelta( idx, metaData.filesChanged)
    }
    commitDeltas
  }
}
