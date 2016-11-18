package com.rossabaker
package benchmarks

import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@Fork(2)
@Measurement(iterations = 10)
@Warmup(iterations = 10)
@Threads(1)
class WordCount extends BenchmarkUtils {
  @Benchmark
  def fs2Sequential: Map[String, Int] = {
    import _root_.fs2._, Stream._
    import java.nio.file.Paths
    io.file.readAll[Task](Paths.get("testdata/lorem-ipsum.txt"), 4096)
      .through(text.utf8Decode andThen text.lines)
      .flatMap(line => emits(line.toLowerCase.split("""\W+""")))
      .filter(_.nonEmpty)
      .fold(Map.empty[String, Int]) { (acc, word) =>
        acc.updated(word, acc.getOrElse(word, 0) + 1)
      }
      .runLast
      .unsafeRun
      .get
  }

  @Benchmark
  def scalazStreamSequential: Map[String, Int] = {
    import scalaz.stream._, Process._
    import java.nio.file.Paths
    constant(4096)
      .through(nio.file.chunkR("testdata/lorem-ipsum.txt"))
      .pipe(text.utf8Decode)
      .pipe(text.lines())
      .flatMap(line => emitAll(line.toLowerCase.split("""\W+""")))
      .filter(_.nonEmpty)
      .fold(Map.empty[String, Int]) { (acc, word) =>
        acc.updated(word, acc.getOrElse(word, 0) + 1)
      }
      .runLast
      .unsafePerformSync
      .get
  }

  @Benchmark
  def monixSequential: Map[String, Int] = {
    import scala.concurrent._, duration._
    import monix.reactive.Observable
    import java.io._
    Await.result(
      Observable.fromLinesReader(
        new BufferedReader(
          new FileReader("testdata/lorem-ipsum.txt")))
        .flatMap(line => Observable.fromIterable(
          line.toLowerCase.split("""\W+""")))
        .filter(_.nonEmpty)
        .foldLeftL(Map.empty[String, Int]) { (acc, word) =>
          acc.updated(word, acc.getOrElse(word, 0) + 1)
        }
        .runAsync(singleThreadedMonixScheduler),
      Duration.Inf
    )
  }
}
