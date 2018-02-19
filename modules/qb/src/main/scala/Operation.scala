package doobie.labs.qb

import scala.annotation.implicitNotFound

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

/** An alias for <:< that gives a slightly more useful error message. */
@implicitNotFound("Operation ${O} isn't permitted here. Allowed: ${S}")
final class HasOperation[S <: Operation, O <: Operation] private ()
object HasOperation {
  implicit def can[S <: Operation, O <: Operation](
    implicit ev: S <:< O
  ): HasOperation[S, O] = {
    void(ev)
    new HasOperation[S, O]()
  }
}
