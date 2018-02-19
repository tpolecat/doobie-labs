package doobie.labs.qb

sealed trait Operation
object Operation {
  type Alias <: Operation
  type Join <: Operation
  type Where <: Operation
  type Select <: Operation
  type Distinct <: Operation
  type GroupBy <: Operation
  type Having <: Operation
  type Done <: Operation
}