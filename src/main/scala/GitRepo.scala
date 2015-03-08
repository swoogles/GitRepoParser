import java.nio.file.Path
import java.nio.file.Paths

case class GitRepo(path: Path, home: Path) {
  def fileName(): String = {
    path.toString.replaceAll("/","_")
  }
  def repoDir(): String = {
    home + "/" + path.toString + "/"
  }
}
object GitRepo {
  def apply(pathString: String, homeString: String) = {
    new GitRepo( Paths.get(pathString), Paths.get(homeString))
  }
}
