package com.rossabaker
package benchmarks

import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@Fork(2)
@Measurement(iterations = 10)
@Warmup(iterations = 10)
@Threads(1)
class Text extends BenchmarkUtils {
  val LoremIpsum: String =
    scala.io.Source.fromFile("testdata/lorem-ipsum.txt")
      .getLines
      .mkString("\n")

  val LoremIpsumBytes: Array[Byte] =
    LoremIpsum.getBytes(java.nio.charset.StandardCharsets.UTF_8)

  @Benchmark
  def fs2EncodeDecode(): String = {
    import _root_.fs2._, Stream._
    emit(LoremIpsum)
      .covary[Task]
      .through(text.utf8Encode)
      .through(text.utf8Decode)
      .runFold("")(_ + _)
      .unsafeRun
  }

  @Benchmark
  def fs2Wc(): Int = {
    import _root_.fs2._, Stream._
    emit(LoremIpsum)
      .covary[Task]
      .through(text.lines)
      .fold(0)((acc, _) => acc + 1)
      .runLast
      .unsafeRun
      .getOrElse(0)
  }

  @Benchmark
  def scalazStreamEncodeDecode(): String = {
    import _root_.scalaz.stream._, Process._
    import scodec.bits.ByteVector
    import scalaz.std.string._
    emit(LoremIpsum)
      .toSource
      .pipe(text.utf8Encode)
      .pipe(text.utf8Decode)
      .runFoldMap(identity)
      .unsafePerformSync
  }

  @Benchmark
  def scalazStreamWc(): Int = {
    import _root_.scalaz.stream._, Process._
    import scodec.bits.ByteVector
    import scalaz.std.anyVal._
    emit(LoremIpsum)
      .toSource
      .pipe(text.lines())
      .fold(0)((acc, _) => acc + 1)
      .runLastOr(0)
      .unsafePerformSync
  }
}
