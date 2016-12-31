package com.rossabaker
package benchmarks

import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@Fork(2)
@Measurement(iterations = 10)
@Warmup(iterations = 10)
@Threads(1)
class Cp extends BenchmarkUtils {
  @Benchmark
  def fs2Sync(): Unit = {
    import _root_.fs2._, Stream._
    import java.nio.file.Paths
    io.file.readAll[Task](Paths.get("testdata/lorem-ipsum.txt"), 4096)
      .to(io.file.writeAll[Task](Paths.get("out/lorem-ipsum.txt")))
      .run
      .unsafeRun
  }

  @Benchmark
  def fs2Async(): Unit = {
    import _root_.fs2._, Stream._
    import java.nio.file.Paths
    io.file.readAllAsync[Task](Paths.get("testdata/lorem-ipsum.txt"), 4096)
      .to(io.file.writeAllAsync[Task](Paths.get("out/lorem-ipsum.txt")))
      .run
      .unsafeRun
  }

  @Benchmark
  def scalazStreamIo(): Unit = {
    import _root_.scalaz.stream._, Process._
    constant(4096)
      .through(io.fileChunkR("testdata/lorem-ipsum.txt"))
      .to(io.fileChunkW("out/lorem-ipsum.txt"))
      .run
      .unsafePerformSync
  }

  @Benchmark
  def scalazStreamNio(): Unit = {
    import _root_.scalaz.stream._, Process._
    constant(4096)
      .through(nio.file.chunkR("testdata/lorem-ipsum.txt"))
      .to(nio.file.chunkW("out/lorem-ipsum.txt"))
      .run
      .unsafePerformSync
  }

  /** https://gist.github.com/alexandru/0e2290a7b5dd8de61ea3ab50e0e08627 */
  @Benchmark
  def monixIo(): Unit = {
    import java.io.{File, FileInputStream, FileOutputStream}
    import monix.eval.Task
    import monix.execution.Ack
    import monix.execution.Ack.{Continue, Stop}
    import monix.reactive.{Consumer, Observable, Observer}
    import scala.concurrent._, duration._
    import scala.util.control.NonFatal

    def copyFile(input: File, destination: File, chunkSize: Int): Task[Unit] =
      Task.defer {
        val in = new FileInputStream(input)
        Observable.fromInputStream(in, chunkSize)
          .consumeWith(fileConsumer(destination))
      }

    def fileConsumer(outputFile: File): Consumer[Array[Byte], Unit] =
      Consumer.create { (scheduler, _, callback) =>
        new Observer.Sync[Array[Byte]] {
          private[this] val out = new FileOutputStream(outputFile)

          def onNext(chunk: Array[Byte]): Ack = {
            try {
              out.write(chunk)
              Continue
            } catch {
              case NonFatal(ex) =>
                try out.close() catch { case NonFatal(_) => /* ignore */ }
                callback.onError(ex)
                Stop
            }
          }

          def onError(ex: Throwable): Unit = {
            try out.close() catch { case NonFatal(_) => /* ignore */ }
            callback.onError(ex)
          }

          def onComplete(): Unit = {
            try {
              out.close()
              callback.onSuccess(())
            } catch {
              case NonFatal(ex) =>
                callback.onError(ex)
            }
          }
        }
      }

    Await.result(
      copyFile(new File("testdata/lorem-ipsum.txt"), new File("out/lorem-ipsum.txt"), 4096)
        .runAsync(monixScheduler),
      Duration.Inf
    )
  }
}
