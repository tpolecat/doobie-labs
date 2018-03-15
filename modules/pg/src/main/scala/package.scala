package doobie.labs.qb

import scala.language.dynamics
import shapeless.{ HNil, :: }

package object pg extends {

  type XString = String with Singleton

  def void(as: Any*): Unit = (as, ())._2

  /**
   * Dynamic syntax for constructing a parameter. `?::int4` is the same as `PgExpr.param["int4"]`.
   * This is not great as it relies on postfix ops and gives poor error messages when parens are
   * needed.
   */
  object ?:: extends Dynamic {
    def selectDynamic[U <: XString](u: U): PgExpr[U :: HNil, HNil, HNil, HNil, U] =
      PgExpr.param[U](new ValueOf(u))
  }

}