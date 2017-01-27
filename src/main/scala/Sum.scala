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
  def fs2Unfold: Long = {
    import _root_.fs2._
    Stream.unfold(0L) { l =>
      if (l < 10000) Some((l, l + 1L))
      else None
    }
      .sum
      .covary[Task]
      .runLast
      .unsafeRun
      .get
  }

  @Benchmark
  def fs2IterateEval: Long = {
    import _root_.fs2._
    Stream.iterateEval(0L)(i => Task.delay(i + 1L))
      .take(size)
      .sum
      .runLast
      .unsafeRun
      .get
  }

  @Benchmark
  def fs2IterateFold: Long = {
    import _root_.fs2._
    Stream.iterate(0L)(_ + 1L)
      .take(size)
      .covary[Task]
      .runFold(0L)(_ + _)
      .unsafeRun
  }

  @Benchmark
  def fs2PureIterate: Long = {
    import _root_.fs2._
    Stream.iterate(0L)(_ + 1L)
      .take(size)
      .sum
      .toList
      .head
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
  def scalazStreamIterateEval: Long = {
    import scalaz.concurrent.Task
    import scalaz.stream._
    Process.iterateEval(0L)(i => Task.delay(i + 1L))
      .take(size)
      .sum
      .runLast
      .unsafePerformSync
      .get
  }

  @Benchmark
  def scalazStreamIterateFold: Long = {
    import scalaz.stream._
    import scalaz.std.anyVal._
    Process.iterate(0L)(_ + 1L)
      .take(size)
      .toSource
      .runFoldMap(identity)
      .unsafePerformSync
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
  def scalazStreamUnfold: Long = {
    import scalaz.stream._
    Process.unfold(0L) { l =>
      if (l < 10000) Some((l, l + 1L))
      else None
    }
      .sum
      .toSource
      .runLast
      .unsafePerformSync
      .get
  }

  @Benchmark
  def monixObservableFromStateActionGlobal: Long = {
    import scala.concurrent._, duration._
    import monix.eval._
    import monix.reactive.Observable
    Await.result(
      Observable.fromStateAction[Long, Long](l => (l, l + 1L))(0L)
        .take(size)
        .sumL
        .runAsync(monixScheduler),
      Duration.Inf
    )
  }

  @Benchmark
  def monixObservableFromAsyncStateActionGlobal: Long = {
    import scala.concurrent._, duration._
    import monix.eval._
    import monix.execution.ExecutionModel
    import monix.reactive.Observable
    Await.result(
      Observable.fromAsyncStateAction[Long, Long](l => Task.delay((l, l + 1L)))(0L)
        .take(size)
        .sumL
        .runAsync(monixScheduler.withExecutionModel(ExecutionModel.AlwaysAsyncExecution)),
      Duration.Inf
    )
  }

  @Benchmark
  def monixObservableFromStateActionSingleThreaded: Long = {
    import scala.concurrent._, duration._
    import monix.eval._
    import monix.reactive.Observable
    Await.result(
      Observable.fromStateAction[Long, Long](l => (l, l + 1L))(0L)
        .take(size)
        .sumL
        .runAsync(singleThreadedMonixScheduler),
      Duration.Inf
    )
  }

  @Benchmark
  def monixObservableFromIterable: Long = {
    import scala.concurrent._, duration._
    import monix.eval._
    import monix.reactive.Observable
    Await.result(
      Observable.fromIterable(0L until size)
        .sumL
        .runAsync(monixScheduler),
      Duration.Inf
    )
  }

  @Benchmark
  def scalaStream: Long = {
    Stream.iterate(0L)(_ + 1L)
      .take(size)
      .sum
  }
}
