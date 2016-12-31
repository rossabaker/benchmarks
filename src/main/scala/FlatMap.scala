package com.rossabaker
package benchmarks

import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@Fork(2)
@Measurement(iterations = 10)
@Warmup(iterations = 10)
@Threads(1)
class FlatMap extends BenchmarkUtils {
  @Param(Array("10000"))
  var size: Int = _

  @Benchmark
  def fs2Now: Int = {
    import fs2._
    def loop(i: Int): Task[Int] =
      if (i < size) Task.now(i + 1).flatMap(loop)
      else Task.now(i)
    Task.now(0).flatMap(loop).unsafeRun()
  }

  @Benchmark
  def fs2Delay: Int = {
    import fs2._
    def loop(i: Int): Task[Int] =
      if (i < size) Task.delay(i + 1).flatMap(loop)
      else Task.delay(i)
    Task.delay(0).flatMap(loop).unsafeRun()
  }

  @Benchmark
  def fs2Apply: Int = {
    import fs2._
    def loop(i: Int): Task[Int] =
      if (i < size) Task(i + 1).flatMap(loop)
      else Task(i)
    Task(0).flatMap(loop).unsafeRun()
  }

  @Benchmark
  def scalazNow: Int = {
    import scalaz.concurrent._
    def loop(i: Int): Task[Int] =
      if (i < size) Task.now(i + 1).flatMap(loop)
      else Task.now(i)
    Task.now(0).flatMap(loop).unsafePerformSync
  }

  @Benchmark
  def scalazDelay: Int = {
    import scalaz.concurrent._
    def loop(i: Int): Task[Int] =
      if (i < size) Task.delay(i + 1).flatMap(loop)
      else Task.delay(i)
    Task.delay(0).flatMap(loop).unsafePerformSync
  }

  @Benchmark
  def scalazApply: Int = {
    import scalaz.concurrent._
    def loop(i: Int): Task[Int] =
      if (i < size) Task(i + 1).flatMap(loop)
      else Task(i)
    Task(0).flatMap(loop).unsafePerformSync
  }

  @Benchmark
  def monixNow: Int = {
    import scala.concurrent._, duration._
    import monix.eval._
    def loop(i: Int): Task[Int] =
      if (i < size) Task.now(i + 1).flatMap(loop)
      else Task.now(i)
    Task.now(0).flatMap(loop).runSyncMaybe.right.get
  }

  @Benchmark
  def monixDelay: Int = {
    import scala.concurrent._, duration._
    import monix.eval._    
    def loop(i: Int): Task[Int] =
      if (i < size) Task.eval(i + 1).flatMap(loop)
      else Task.eval(i)
    Task.delay(0).flatMap(loop).runSyncMaybe.right.get
  }

  @Benchmark
  def monixApply: Int = {
    import scala.concurrent._, duration._
    import monix.eval._
    def loop(i: Int): Task[Int] =
      if (i < size) Task(i + 1).flatMap(loop)
      else Task(i)
    Await.result(Task(0).flatMap(loop).runAsync, Duration.Inf)
  }

  @Benchmark
  def futureSuccessful: Int = {
    import scala.concurrent._, duration._, ExecutionContext.Implicits.global
    def loop(i: Int): Future[Int] =
      if (i < size) Future.successful(i + 1).flatMap(loop)
      else Future.successful(i)
    Await.result(Future.successful(0).flatMap(loop), Duration.Inf)
  }

  @Benchmark
  def futureApply: Int = {
    import scala.concurrent._, duration._, ExecutionContext.Implicits.global
    def loop(i: Int): Future[Int] =
      if (i < size) Future(i + 1).flatMap(loop)
      else Future(i)
    Await.result(Future(0).flatMap(loop), Duration.Inf)
  }

  @Benchmark
  def futureTrampolineEc: Int = {
    import scala.concurrent._, duration._, ExecutionContext.Implicits.global
    def loop(i: Int): Future[Int] =
      if (i < size) Future.successful(i + 1).flatMap(loop)
      else Future.successful(i)
    Await.result(Future.successful(0).flatMap(loop), Duration.Inf)
  }
}
