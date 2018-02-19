package doobie.labs.qb

import doobie.labs.qb.proof._
import scala.annotation.implicitNotFound
import shapeless._

final class Statement[S <: Operation, E <: HList] private (val sql: String)(
  implicit b: AliasedBindings[E] // unused
) {
  import Operation._
  void(b)

  type Can[O <: Operation] = Statement.HasOperation[S, O]

  def crossJoin[A <: XString, Eʹ <: HList](t: Table[A, Eʹ])(
    implicit ab: AliasedBindings[(A, Eʹ) :: E],
             st: Can[Join]
  ): Statement[S, (A, Eʹ) :: E] = {
    void(st)
    new Statement(s"${sql} CROSS JOIN ${t.sql}")
  }

  def innerJoin[A <: XString, Eʹ <: HList](t: Table[A, Eʹ])(
    implicit ab: AliasedBindings[(A, Eʹ) :: E],
             st: Can[Join]
  ): Joiner[S, (A, Eʹ) :: E] = {
    void(st)
    new Joiner(cond => new Statement(s"${sql} INNER JOIN ${t.sql} ON $cond"))
  }

  // Statement conditions need a lowered environment with no options at all because they don't have
  // any meaning in SQL. I wonder if they should be SQL types (!)
  // It doesn't really matter since Expr ends up being mostly untyped.
  def leftJoin[A <: XString, Eʹ <: HList, Eʹʹ <: HList](t: Table[A, Eʹ])(
    implicit be: Bindings.Aux[Eʹ, Eʹʹ],
             ab: AliasedBindings[(A, Eʹʹ) :: E],
                          st: Can[Join]

  ): Joiner[S, (A, Eʹʹ) :: E] = {
    void(be, st)
    new Joiner(cond => new Statement(s"${sql} LEFT JOIN ${t.sql} ON $cond"))
  }

  class Joiner[S0 <: Operation, E0 <: HList](mkJoin: String => Statement[S0, E0])(
    implicit ev: AliasedBindings[E0]
  ) {
    def on(f: AliasedEnv[E0] => Expr[Boolean]): Statement[S0, E0] =
      mkJoin(f(new AliasedEnv[E0]).sql)
  }

  def where(f: AliasedEnv[E] => Expr[Boolean])(
    implicit ca: Can[Where]
  ): Statement[Select, E] = {
    void(ca)
    val cond = f(new AliasedEnv[E]).sql
    new Statement(s"$sql WHERE $cond")
  }

  override def toString =
    s"Statement($sql)"

}
object Statement {
  import Operation._

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

  def fromTable[A <: XString, E <: HList](t: Table[A, E])(
    implicit ab: AliasedBindings[(A, E) :: HNil]
  ): Statement[Join with Where with Select, (A, E) :: HNil] =
    new Statement(s"FROM ${t.sql}")

}
