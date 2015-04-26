package com.billding.git

import akka.actor.{ ActorSystem, Actor}

case class RepoAndAction (
  repo: Repo, 
  commitAction: CommitAction
)

object GitManager {
  val home = "/home/bfrasure/"

  def main(args: Array[String]) = 
  {
    val email = args(0).split("\\s+")(0)
    println(s"email: ${email}")
    val action = args(0).split("\\s+")(1)
    println(s"action: ${action}") 

    val availableActions = Map(
      "filesChanged" -> FilesChanged,
    "LineDeltas" -> LineDeltas
  )

    val chosenAction: Option[CommitAction] = availableActions.get(action)
    println(s"chosenAction: ${chosenAction}")

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


    //val commitAction: CommitAction = FilesChanged

    chosenAction match {
      case Some(commitAction) => {
        val system = ActorSystem("helloakka")
        val numRepos = repos.size
        val filesPerRepo = 2
        val filesToWrite = numRepos * filesPerRepo
        val dispatcher = system.actorOf(GitDispatcher.props(filesToWrite), "dispatcher")
        for { repo <- repos } 
        { dispatcher ! RepoAndAction(repo, commitAction) }
      }
      case None =>  {
        println("Bad Action. I'm not taking another step.")
      }
    }

  }
}
