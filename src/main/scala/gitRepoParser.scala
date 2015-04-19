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
     Repo(x, home, gitHashes)
    }

    val system = ActorSystem("helloakka")
    val numRepos = repos.size
    val filesPerRepo = 2
    val filesToWrite = numRepos * filesPerRepo
    val dispatcher = system.actorOf(GitDispatcher.props(filesToWrite), "dispatcher")

    for {
      repo <- gitReposWithoutHashes
    } {
      println("Repo: " + repo)

    implicit val program = Seq("git")
    val loggerArguments = Seq("--git-dir="+repo.dir+".git", "log", "--oneline")
    val logOutput: Array[String] = com.billding.SystemCommands.runFullCommand(loggerArguments).split("\n")

    //logOutput foreach { println }
    println("Log output: " + logOutput)
    println("NumHashes: " + logOutput.length)
    val hashes: List[GitHash]=  logOutput map { x =>  GitHash(x.split("\\s")(0))} toList

      //val repoTarget = RepoTarget(repo.copy(hashes=hashes), email)
      //dispatcher ! repoTarget
      dispatcher ! repo.copy(hashes=hashes)
    }

  }
}
