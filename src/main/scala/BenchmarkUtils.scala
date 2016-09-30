/* Derived from https://github.com/functional-streams-for-scala/fs2/blob/a2a4bfd7cd37790cccecff99d50a6c425af9776f/benchmark/src/main/scala/fs2/benchmark/utils.scala */
package com.rossabaker
package benchmarks

/** helper trait with utils */
trait BenchmarkUtils {
  /**
    Number of cores. Note that hyperthreading causes this to be multiplied by 2,
    but isn't twice the performance
    */
  val cores: Int = Runtime.getRuntime().availableProcessors

  /** strategy scaled to number of cores */
  implicit lazy val scaledFs2Strategy: fs2.Strategy =
    fs2.Strategy.fromFixedDaemonPool(cores)

  /** strategy scaled to number of cores */
  implicit lazy val scaledZStrategy: scalaz.concurrent.Strategy =
    scalaz.concurrent.Strategy.DefaultStrategy
}
