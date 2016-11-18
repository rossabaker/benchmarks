package com.rossabaker
package benchmarks

import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@Fork(2)
@Measurement(iterations = 10)
@Warmup(iterations = 10)
@Threads(1)
class Compress extends BenchmarkUtils {
  val LoremIpsum: Array[Byte] =
    scala.io.Source.fromFile("testdata/lorem-ipsum.txt")
      .getLines
      .mkString("\n")
      .getBytes

  val LoremIpsumStream: fs2.Stream[fs2.Task, Byte] =
    fs2.Stream.chunk(fs2.Chunk.bytes(LoremIpsum))

  val LoremIpsumProcess: scalaz.stream.Process[scalaz.concurrent.Task, scodec.bits.ByteVector] =
    scalaz.stream.Process.emit(scodec.bits.ByteVector.view(LoremIpsum))

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
  def fs2NoIo() = {
    import _root_.fs2._, Stream._
    LoremIpsumStream
      .through(compress.deflate(nowrap = true))
      .through(compress.inflate(nowrap = true))
      .run
      .unsafeRun
  }

  @Benchmark
  def fs2Text() = {
    import _root_.fs2._, Stream._
    LoremIpsumStream
      .through(text.utf8Decode)
      .through(text.utf8Encode)
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

  @Benchmark
  def scalazStreamNoIo() = {
    import _root_.scalaz.stream._, Process._
    LoremIpsumProcess
      .pipe(compress.deflate(nowrap = true))
      .pipe(compress.inflate(nowrap = true))
      .run
      .unsafePerformSync
  }

  @Benchmark
  def scalazStreamText() = {
    import _root_.scalaz.stream._, Process._
    LoremIpsumProcess
      .pipe(text.utf8Decode)
      .pipe(text.utf8Encode)
      .run
      .unsafePerformSync
  }

}
