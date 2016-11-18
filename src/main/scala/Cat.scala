package com.rossabaker
package benchmarks

import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@Fork(2)
@Measurement(iterations = 10)
@Warmup(iterations = 10)
@Threads(1)
class Cat extends BenchmarkUtils {
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
}
