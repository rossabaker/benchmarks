import sbt._

import scala.util.{Either, Left, Right}

trait Fs2Refs {
  def repo: Either[URI, File]

  lazy val fs2Core = repo.fold(ProjectRef(_, "coreJVM"), ProjectRef(_, "coreJVM"))
  lazy val fs2IO = repo.fold(ProjectRef(_, "io"), ProjectRef(_, "io"))
}

object github extends Fs2Refs {
  val repo = Left(uri("git://github.com/functional-streams-for-scala/fs2.git#01ec76b"))
}

object local extends Fs2Refs {
  val repo = Right(file("..") / "fs2")
}
