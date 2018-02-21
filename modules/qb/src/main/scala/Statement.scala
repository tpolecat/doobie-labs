package doobie.labs.qb

import doobie._, doobie.implicits._
import doobie.labs.qb.proof._
import doobie.util.fragment.Fragment
import shapeless.{ HList, ::, HNil }

final class Statement[S <: Statement.Operation, E <: HList] private (val sql: Fragment)(
  implicit b: AliasedBindings[E] // unused
) {
  void(b)

  import Statement._
  import Statement.Operation._
  import Selection.Operation._

  type Can[O <: Operation] = HasOperation[S, O]

  def crossJoin[A <: XString, Eʹ <: HList](t: Table[A, Eʹ])(
    implicit ab: AliasedBindings[(A, Eʹ) :: E],
             st: Can[Join]
  ): Statement[S, (A, Eʹ) :: E] = {
    void(st)
    new Statement(sql ++ frNL ++ fr"CROSS JOIN ${t.sql}")
  }

  def innerJoin[A <: XString, Eʹ <: HList](t: Table[A, Eʹ])(
    implicit ab: AliasedBindings[(A, Eʹ) :: E],
             st: Can[Join]
  ): Joiner[S, (A, Eʹ) :: E] = {
    void(st)
    new Joiner(cond => new Statement(sql ++ frNL ++ fr"INNER JOIN " ++ Fragment.const(t.sql) ++ fr"ON" ++ cond))
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
    new Joiner(cond => new Statement(sql ++ frNL ++ fr"LEFT JOIN" ++ Fragment.const(t.sql) ++ fr"ON" ++ cond))
  }

  def where(f: AliasedEnv[E] => Expr[Boolean])(
    implicit ca: Can[Where]
  ): Statement[Select, E] = {
    void(ca)
    val cond = f(new AliasedEnv[E]).sql
    new Statement(sql ++ frNL ++ fr"WHERE" ++ cond)
  }

  // TODO: allow column aliasing, and add aliases to E

  // We need to accumulate columns that are mentioned in aggregate functions

  // https://www.postgresql.org/docs/9.6/static/sql-select.html#SQL-GROUPBY
  //
  // When GROUP BY is present, or any aggregate functions are present, it is not valid for the
  // SELECT list expressions to refer to ungrouped columns except within aggregate functions or
  // when the ungrouped column is functionally dependent on the grouped columns, since there would
  // otherwise be more than one possible value to return for an ungrouped column. A functional
  // dependency exists if the grouped columns (or a subset thereof) are the primary key of the
  // table containing the ungrouped column.

  // we need to accumulate grouped (aggregate) and ungrouped columns in SELECT
  // if there are any grouped columns, all ungrouped columns must be mentioned in GROUP BY
  // and only these GROUP BY columns (or aggregates) can appear in the HAVING clause

  def select[B <: HList, O <: HList](f: AliasedEnv[E] => B)(
    implicit ev: Output.Aux[B, O],
             co: Composite[O]
  ): Selection[Distinct with GroupBy with OrderBy, E, O] = {
    val sel = f(new AliasedEnv[E])
    val fra = ev.sql(sel) ++ sql
    Selection.fromFragment(fra)
  }

  // TODO: selectAll

  override def toString =
    s"Statement($sql)"

}
object Statement {

  sealed trait Operation
  object Operation {
    type Join   <: Operation
    type Where  <: Operation
    type Select <: Operation
  }
  import Operation._

  def fromTable[A <: XString, E <: HList](t: Table[A, E])(
    implicit ab: AliasedBindings[(A, E) :: HNil]
  ): Statement[Join with Where with Select, (A, E) :: HNil] =
    new Statement(frNL ++ fr"FROM" ++ Fragment.const(t.sql))

  class Joiner[S0 <: Operation, E0 <: HList](mkJoin: Fragment => Statement[S0, E0])(
    implicit ev: AliasedBindings[E0]
  ) {
    def on(f: AliasedEnv[E0] => Expr[Boolean]): Statement[S0, E0] =
      mkJoin(f(new AliasedEnv[E0]).sql)
  }

}
