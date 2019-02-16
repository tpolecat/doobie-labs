package doobie.labs.qb

import doobie._, doobie.implicits._
import doobie.labs.qb.proof._

import shapeless._

final class Selection[S <: Selection.Operation, E <: HList, A <: HList] private (
  val from: Fragment,
  val select: Fragment
)(
  implicit ae: AliasedBindings[E],
           ra: Read[A],
          //  wa: Write[A]
) {

  import Selection.Operation
  import Selection.Operation._

  type Can[O <: Operation] = HasOperation[S, O]

  def sql: Fragment = select ++ from

  def distinct(
    implicit ev: Can[Distinct]
  ): Selection[GroupBy with OrderBy, E, A] = {
    void(ev)
    new Selection(from, fr"SELECT DISTINCT")
  }

  def distinctBy[B <: HList, O <: HList](f: AliasedEnv[E] => B)(
    implicit ev: Output.Aux[B, O],
             co: Read[O],
             xx: Can[Distinct]
  ): Selection[GroupBy with OrderBy, E, O] = {
    void(xx)
    val cols = f(new AliasedEnv[E])
    val sel  = fr0"SELECT DISTINCT BY(" ++ ev.sql(cols) ++ fr")"
    new Selection(from, sel)
  }

  def groupBy[B <: HList](f: AliasedEnv[E] => B)(
    implicit ev: Output[B],
             xx: Can[GroupBy]
  ): Selection[Having with OrderBy, E, A] = {
    void(xx)
    val cols = f(new AliasedEnv[E])
    val froʹ = from ++ frNL ++ fr"GROUP BY" ++ ev.sql(cols)
    new Selection(froʹ, select)
  }

  def having(f: AliasedEnv[E] => Expr[Boolean])(
    implicit ev: Can[Having]
  ): Selection[OrderBy, E, A] = {
    void(ev)
    val cond = f(new AliasedEnv[E]).sql
    new Selection(from ++ frNL ++ fr"HAVING" ++ cond, select)
  }

  def orderBy[B <: HList](f: AliasedEnv[E] => B)(
    implicit ev: Output[B],
             xx: Can[OrderBy]
  ): Selection[Nothing, E, A] = {
    void(xx)
    val cols = f(new AliasedEnv[E])
    val froʹ = from ++ frNL ++ fr"ORDER BY" ++ ev.sql(cols)
    new Selection(froʹ, select)
  }

  def done: Query0[A] =
    sql.query[A]

  override def toString =
    s"Selection($sql)"

}

object Selection {

  sealed trait Operation
  object Operation {
    type Distinct <: Operation
    type GroupBy  <: Operation
    type Having   <: Operation
    type OrderBy  <: Operation
  }
  import Operation._

  def fromFragment[E <: HList, A <: HList](select: Fragment)(
    implicit ae: AliasedBindings[E],
             ra: Read[A]
  ): Selection[Distinct with GroupBy with OrderBy, E, A] =
    new Selection(select, fr"SELECT")

}