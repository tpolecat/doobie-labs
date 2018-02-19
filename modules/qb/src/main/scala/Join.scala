package doobie.labs.qb

import doobie.labs.qb.proof._
import shapeless._

final class Join[E <: HList] private (sql: String)(
  implicit b: AliasedBindings[E] // unused
) {

  void(b) // TODO: remove

  def crossJoin[A <: XString, Eʹ <: HList](t: Table[A, Eʹ])(
    implicit ab: AliasedBindings[(A, Eʹ) :: E]
  ): Join[(A, Eʹ) :: E] =
    new Join(s"${sql} CROSS JOIN ${t.sql}")

  def innerJoin[A <: XString, Eʹ <: HList](t: Table[A, Eʹ])(
    implicit ab: AliasedBindings[(A, Eʹ) :: E]
  ): Joiner[(A, Eʹ) :: E] =
    new Joiner(cond => new Join(s"${sql} INNER JOIN ${t.sql} ON $cond"))

  // Join conditions need a lowered environment with no options at all because they don't have
  // any meaning in SQL. I wonder if they should be SQL types (!)
  // It doesn't really matter since Expr ends up being mostly untyped.
  def leftJoin[A <: XString, Eʹ <: HList, Eʹʹ <: HList](t: Table[A, Eʹ])(
    implicit be: Bindings.Aux[Eʹ, Eʹʹ],
             ab: AliasedBindings[(A, Eʹʹ) :: E]
  ): Joiner[(A, Eʹʹ) :: E] = {
    void(be)
    new Joiner(cond => new Join(s"${sql} LEFT JOIN ${t.sql} ON $cond"))
  }

  class Joiner[E0 <: HList](mkJoin: String => Join[E0])(
    implicit ev: AliasedBindings[E0]
  ) {
    def on(f: AliasedEnv[E0] => Expr[Boolean]): Join[E0] =
      mkJoin(f(new AliasedEnv[E0]).sql)
  }

  def where(f: AliasedEnv[E] => Expr[Boolean]): Where[E] = {
    val cond = f(new AliasedEnv[E]).sql
    new Where(s"$sql WHERE $cond")
  }

  override def toString =
    s"Join($sql)"

}
object Join {

  def fromTable[A <: XString, E <: HList](t: Table[A, E])(
    implicit ab: AliasedBindings[(A, E) :: HNil]
  ): Join[(A, E) :: HNil] =
    new Join(s"FROM ${t.sql}")

}
