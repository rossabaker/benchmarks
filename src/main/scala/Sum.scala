package com.rossabaker
package benchmarks

import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@Fork(2)
@Measurement(iterations = 10)
@Warmup(iterations = 10)
@Threads(1)
class Sum extends BenchmarkUtils {
  @Param(Array("10000"))
  var size: Int = _

  @Benchmark
  def fs2Iterate: Long = {
    import _root_.fs2._
    Stream.iterate(0L)(_ + 1L)
      .take(size)
      .sum
      .covary[Task]
      .runLast
      .unsafeRun
      .get
  }

  @Benchmark
  def fs2Chunked: Long = {
    import _root_.fs2._
    Stream.chunk(Chunk.seq(0 until size))
      .sum
      .covary[Task]
      .runLast
      .unsafeRun
      .get
  }

  @Benchmark
  def scalazStreamIterate: Long = {
    import scalaz.stream._
    Process.iterate(0L)(_ + 1L)
      .take(size)
      .sum
      .toSource
      .runLast
      .unsafePerformSync
      .get
  }

  @Benchmark
  def scalazStreamEmitAll: Long = {
    import scalaz.stream._
    Process.emitAll(0L until size)
      .sum
      .toSource
      .runLast
      .unsafePerformSync
      .get
  }

  @Benchmark
  def monixObservableFromStateAction: Long = {
    import scala.concurrent._, duration._
    import monix.eval._
    import monix.execution.Scheduler.Implicits.global
    import monix.reactive.Observable
    Await.result(
      Observable.fromStateAction[Long, Long](l => (l, l + 1L))(0L)
        .take(size)
        .sumL
        .runAsync(monix.execution.Scheduler.Implicits.global),
      Duration.Inf
    )
  }


  @Benchmark
  def monixObservableFromIterable: Long = {
    import scala.concurrent._, duration._
    import monix.eval._
    import monix.execution.Scheduler.Implicits.global
    import monix.reactive.Observable
    Await.result(
      Observable.fromIterable(0L until size)
        .sumL
        .runAsync(monix.execution.Scheduler.Implicits.global),
      Duration.Inf
    )
  }
}
