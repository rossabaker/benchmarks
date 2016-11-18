package com.rossabaker
package benchmarks

import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@Fork(2)
@Measurement(iterations = 10)
@Warmup(iterations = 10)
@Threads(1)
class Compress extends BenchmarkUtils {
  @Benchmark
  def fs2GzipGunzip(): Unit = {
    import _root_.fs2._, Stream._
    import java.nio.file.Paths
    io.file.readAll[Task](Paths.get("testdata/lorem-ipsum.txt"), 4096)
      .through(compress.deflate(nowrap = true))
      .through(compress.inflate(nowrap = true))
      .to(io.file.writeAll[Task](Paths.get("out/lorem-ipsum.txt")))
      .run
      .unsafeRun
  }

  @Benchmark
  def fs2Gzip(): Unit = {
    import _root_.fs2._, Stream._
    import java.nio.file.Paths
    io.file.readAll[Task](Paths.get("testdata/lorem-ipsum.txt"), 4096)
      .through(compress.deflate(nowrap = true))
      .to(io.file.writeAll[Task](Paths.get("out/lorem-ipsum.txt.gz")))
      .run
      .unsafeRun
  }

  @Benchmark
  def scalazStreamGzipGunzip(): Unit = {
    import _root_.scalaz.stream._, Process._
    constant(4096)
      .through(nio.file.chunkR("testdata/lorem-ipsum.txt"))
      .pipe(compress.deflate(nowrap = true))
      .pipe(compress.inflate(nowrap = true))
      .to(nio.file.chunkW("out/lorem-ipsum.txt"))
      .run
      .unsafePerformSync
  }

  @Benchmark
  def scalazStreamGzip(): Unit = {
    import _root_.scalaz.stream._, Process._
    constant(4096)
      .through(nio.file.chunkR("testdata/lorem-ipsum.txt"))
      .pipe(compress.deflate(nowrap = true))
      .to(nio.file.chunkW("out/lorem-ipsum.txt.gz"))
      .run
      .unsafePerformSync
  }
}
