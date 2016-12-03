import github._

lazy val root = project.in(file(".")).dependsOn(fs2Core, fs2IO)

scalaVersion := "2.12.0"

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
//  "-Yinline-warnings",
  "-Ywarn-dead-code",
  "-Xfuture"
)

libraryDependencies ++= Seq(
  "io.monix" %% "monix-reactive" % "2.1.1",
  "org.scalaz.stream" %% "scalaz-stream" % "0.8.6a"
)

resolvers += Resolver.sonatypeRepo("snapshots")

initialCommands := "import com.rossabaker.benchmarks._"

enablePlugins(JmhPlugin)
