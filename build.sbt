scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yinline-warnings",
  "-Ywarn-dead-code",
  "-Xfuture"
)

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "0.9.1",
  "io.monix" %% "monix-reactive" % "2.0.2",
  "org.scalaz.stream" %% "scalaz-stream" % "0.8.4a"
)

initialCommands := "import com.rossabaker.benchmarks._"

enablePlugins(JmhPlugin)
