
case class GitRepo(path: String, home: String) {
  def fileName(): String = {
    path.replaceAll("/","_")
  }
  def repoDir(): String = {
    home + path + "/"
  }
}
