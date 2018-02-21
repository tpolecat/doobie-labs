package doobie.labs.qb

import doobie.labs.qb.proof._
import doobie.Fragment
import shapeless._
import scala.language.dynamics

/** An environment where the keys are strings and the values are Bindings. */
class AliasedEnv[E <: HList](
  implicit ae: AliasedBindings[E] // unused for now
) extends Dynamic {
  void(ae)
  def selectDynamic[V <: HList](s: String)(
    implicit ev: HasField.Aux[E, s.type, V],
             bv: Bindings[V]
  ): Env[V] = {
    void(ev)
    new Env[V](s)
  }
}

/** An environment where the keys are strings and the values are types. */
class Env[E <: HList](alias: String)(
  implicit be: Bindings[E] // unused but helps keep things straight
) extends Dynamic {
  void(be)
  def selectDynamic(s: String)(
    implicit ev: HasField[E, s.type]
  ): Expr[ev.Out] =
    new Expr[ev.Out] {
      def sql = Fragment.const0(s"$alias.$s")
    }
}

