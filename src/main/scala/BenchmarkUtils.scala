/* Derived from https://github.com/functional-streams-for-scala/fs2/blob/a2a4bfd7cd37790cccecff99d50a6c425af9776f/benchmark/src/main/scala/fs2/benchmark/utils.scala */
/* trampolineEc derived from https://github.com/http4s/blaze/blob/28ddfce96e4630f6f6f1147b2fb1b1283ce25703/core/src/main/scala/org/http4s/blaze/util/Execution.scala#L22-L36 */
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

  lazy val trampolineEc: scala.concurrent.ExecutionContext = {
    import scala.annotation.tailrec
    import scala.concurrent._
    import java.util._

    // Only safe to use from a single thread
    final class ThreadLocalTrampoline extends ExecutionContext {
      private var running = false
      private var r0, r1, r2: Runnable = null
      private var rest: ArrayDeque[Runnable] = null

      override def execute(runnable: Runnable): Unit = {
        if (r0 == null) r0 = runnable
        else if (r1 == null) r1 = runnable
        else if (r2 == null) r2 = runnable
        else {
          if (rest == null) rest = new ArrayDeque[Runnable]()
          rest.add(runnable)
        }

        if (!running) {
          running = true
          run()
        }
      }

      override def reportFailure(cause: Throwable): Unit =
        trampolineFailure(cause)

      @tailrec
      private def run(): Unit = {
        val r = next()
        if (r == null) {
          rest = null     // don't want a memory leak from potentially large array buffers
          running = false
        } else {
          try r.run()
          catch { case e: Throwable => reportFailure(e) }
          run()
        }
      }

      private def next(): Runnable = {
        val r = r0
        r0 = r1
        r1 = r2
        r2 = if (rest != null) rest.pollFirst() else null
        r
      }
    }

    def trampolineFailure(cause: Throwable) = {
      System.err.println("Trampoline EC exception caught")
      cause.printStackTrace(System.err)
    }

    new ExecutionContext {
      private val local = new ThreadLocal[ThreadLocalTrampoline]

      def execute(runnable: Runnable): Unit = {
        var queue = local.get()
        if (queue == null) {
          queue = new ThreadLocalTrampoline
          local.set(queue)
        }

        queue.execute(runnable)
      }

      def reportFailure(t: Throwable): Unit = trampolineFailure(t)
    }
  }

  lazy val singleThreadedMonixScheduler: monix.execution.Scheduler =
    monix.execution.Scheduler(BenchmarkUtils.singleThreadedExecutor)
}

object BenchmarkUtils {
  lazy val singleThreadedExecutor =
    java.util.concurrent.Executors.newSingleThreadScheduledExecutor(DaemonThreadFactory)
}

object DaemonThreadFactory extends java.util.concurrent.ThreadFactory {
  def newThread(r: Runnable): Thread = {
    val t = new Thread(r)
    t.setDaemon(true)
    t
  }
}
