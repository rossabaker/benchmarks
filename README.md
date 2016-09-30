# Benchmarks

Benchmarks for various Scala streaming and task libraries.  Better
name wanted.

# Types covered

## Tasks

* fs2.Task
* monix.eval.Task
* scala.concurrent.Future
* scalaz.concurrent.Task

## Streams

Coming soon.

# Run them yourself

`sbt jmh:run`

# Results

Results are storied in the [results/](results/) directory.  Filenames
correspond to tags.