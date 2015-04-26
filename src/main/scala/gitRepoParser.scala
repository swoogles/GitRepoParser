package com.billding.git

import akka.actor.{ ActorSystem, Actor}

case class RepoAndAction (
  repo: Repo, 
  repoAction: RepoAction
)

object GitManager {
  val home = "/home/bfrasure/"

  def main(args: Array[String]) = 
  {
    val cmdLineArgs: Array[String] = args(0).split("\\s+")

    val email = cmdLineArgs(0)
    val actionParam = cmdLineArgs(1)

    val chosenAction: Option[RepoAction] = RepoAction.availableActions.get(actionParam)

    val repos = List(
      //"AtomicScala",
    //"AudioHand/Mixer",
    "ClashOfClans",
    "ConcurrencyInAction",
    "GitRepoParser",
    "Latex",
    //"Personal",
    //"Physics",
    //"ProjectEuler",
    "RoundToNearestX"
  ) map {  x => Repo("Repositories/" + x , home) }


    chosenAction match {
      case Some(repoAction) => {
        val system = ActorSystem("helloakka")
        val numRepos = repos.size
        val filesPerRepo = 2
        val filesToWrite = numRepos * filesPerRepo
        val dispatcher = system.actorOf(GitDispatcher.props(filesToWrite), "dispatcher")

        for { repo <- repos } { dispatcher ! RepoAndAction(repo, repoAction) }
      }
      case None =>  {
        println(s"\nBad Action: ${actionParam}")
        println(s"\nAvailable Actions:${RepoAction.availableActions.keys.foldLeft("\t")(_ + "\n-" + _)}")
      }
    }

  }
}
