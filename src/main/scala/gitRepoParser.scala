package com.billding.git

import akka.actor.{ ActorSystem, Actor}

case class RepoTarget(repo: Repo, email: String)

object GitManager {
  val home = "/home/bfrasure/"

  def main(args: Array[String]) = 
  {
    val email = args(0)

    val repos = List(
      "AtomicScala",
      "AudioHand/Mixer",
      "ClashOfClans",
      "ConcurrencyInAction",
      "GitRepoParser",
      "Latex",
      "Personal",
      "Physics",
      "ProjectEuler",
      "RoundToNearestX"
    )
    val qualifiedRepos = repos.map { "Repositories/" + _ }

    val gitHashes: List[GitHash] = Nil
    val gitReposWithoutHashes: List[Repo] = qualifiedRepos.map { x=>
      Repo(x, home)
    }

    val system = ActorSystem("helloakka")
    val numRepos = repos.size
    val filesPerRepo = 2
    val filesToWrite = numRepos * filesPerRepo
    val dispatcher = system.actorOf(GitDispatcher.props(filesToWrite), "dispatcher")


    for {
      repo <- gitReposWithoutHashes
      } {
        dispatcher ! repo
      }

  }
}
