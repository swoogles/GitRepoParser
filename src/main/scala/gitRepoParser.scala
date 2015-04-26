package com.billding.git

import akka.actor.{ ActorSystem, Actor}

object GitManager {
  val home = "/home/bfrasure/"

  def main(args: Array[String]) = 
  {
    val email = args(0)

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

    val system = ActorSystem("helloakka")
    val numRepos = repos.size
    val filesPerRepo = 2
    val filesToWrite = numRepos * filesPerRepo
    val dispatcher = system.actorOf(GitDispatcher.props(filesToWrite), "dispatcher")

    for { repo <- repos } 
      { dispatcher ! repo }

  }
}
