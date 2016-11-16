// https://github.com/functional-streams-for-scala/fs2/blob/3522315043ce31beb121c44cec4a7b1ce4e509e3/docs/src/ReadmeExample.md
package com.rossabaker
package benchmarks

import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@Fork(2)
@Measurement(iterations = 10)
@Warmup(iterations = 10)
@Threads(1)
class FahrenheitToCelsius extends BenchmarkUtils {
  @Param(Array("10000"))
  var size: Int = _

  def fahrenheitToCelsius(f: Double): Double =
    (f - 32.0) * (5.0/9.0)

  @Benchmark
  def fs2(): Unit = {
    import _root_.fs2._
    import java.nio.file.Paths
    io.file.readAll[Task](Paths.get("testdata/fahrenheit.txt"), 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
      .map(line => fahrenheitToCelsius(line.toDouble).toString)
      .intersperse("\n")
      .through(text.utf8Encode)
      .through(io.file.writeAll(Paths.get("testdata/celsius.txt")))
      .run
      .unsafeRun
  }

  @Benchmark
  def scalazStream(): Unit = {
    import scalaz.stream._
    import java.nio.file.Paths
    io.linesR("testdata/fahrenheit.txt")
      .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
      .map(line => fahrenheitToCelsius(line.toDouble).toString)
      .intersperse("\n")
      .pipe(text.utf8Encode)
      .to(io.fileChunkW("testdata/celsius.txt"))
      .run
      .unsafePerformSync
  }
}
