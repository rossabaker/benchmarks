Benchmarks for various Scala streaming and task libraries

# Types covered

## Tasks

* fs2.Task
* monix.eval.Task
* scala.concurrent.Future
* scalaz.concurrent.Task

# Run them yourself

`sbt jmh:run`

# Results

Results are storied in the [results/] directory.  Filenames correspond
to tags.