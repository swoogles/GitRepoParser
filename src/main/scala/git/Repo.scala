package com.billding.git

import java.nio.file.Path
import java.nio.file.Paths

case class Repo(path: Path, home: Path, hashes: List[GitHash]) {
  def fileName(): String = {
    path.toString.replaceAll("/","_")
  }
  def dir(): String = {
    home + "/" + path.toString + "/"
  }
}
object Repo {
  def apply(pathString: String, homeString: String, hashes: List[GitHash]) = {
    new Repo( Paths.get(pathString), Paths.get(homeString), hashes)
  }
}
