package doobie.labs.qb
package proof

import scala.annotation.implicitNotFound

/** An alias for <:< that gives a slightly more useful error message. */
@implicitNotFound("Operation ${O} isn't permitted here. Allowed: ${S}")
final class HasOperation[S, O] private ()
object HasOperation {
  implicit def can[S, O](
    implicit ev: S <:< O
  ): HasOperation[S, O] = {
    void(ev)
    new HasOperation[S, O]()
  }
}
