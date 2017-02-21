package com.billding.git

import akka.actor.{Actor, ActorSystem}
import ammonite.ops.Path

import scala.util.{Failure, Success, Try}

case class RepoAndAction (
  repo: Repo, 
  repoAction: RepoAction
)

object GitManager {
  val home = "/home/bfrasure/"

  def analyzeRepo(targetRepo: Path, action: String) = {
    val chosenAction: Try[RepoAction] = RepoAction.getAction(action)
    val repo = Repo(targetRepo)
    perform(chosenAction, List(repo))
  }

  def analyzeRepo(action: String, targetRepos: Seq[Path] ) = {
    val chosenAction: Try[RepoAction] = RepoAction.getAction(action)
    val repos = targetRepos map {  Repo(_) }
    perform(chosenAction, repos)
  }

  def main(args: Array[String]) = 
  {
    val cmdLineArgs: Array[String] = args(0).split("\\s+")

    val email = cmdLineArgs(0)
    val actionParam = cmdLineArgs(1)

    val chosenAction: Try[RepoAction] = RepoAction.getAction(actionParam)

    val repos = List(
      //"AtomicScala",
    //"AudioHand/Mixer",
//    "ClashOfClans",
//    "ConcurrencyInAction",
    "GitRepoParser",
//    "Latex",
    "Personal"
    //"Physics",
    //"ProjectEuler",
//    "RoundToNearestX"
  ) map {  x => Repo(home + "Repositories/" + x) }


    perform(chosenAction, repos)
  }

  private def perform(chosenAction: Try[RepoAction], repos: Seq[Repo] ) = {
    chosenAction match {
      case Success(repoAction) => {
        val system = ActorSystem("helloakka")
        val numRepos = repos.size
        val filesPerRepo = 2
        val filesToWrite = numRepos * filesPerRepo
        val dispatcher = system.actorOf(GitDispatcher.props(filesToWrite), "dispatcher")

        for {repo <- repos} {
          dispatcher ! RepoAndAction(repo, repoAction)
        }
      }
      case Failure(exception) => {
        println(exception.getMessage)
      }
    }
  }
}
