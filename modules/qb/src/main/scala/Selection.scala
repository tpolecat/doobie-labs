package doobie.labs.qb

import doobie._, doobie.implicits._
import doobie.labs.qb.proof._

import shapeless._

final class Selection[S <: Selection.Operation, E <: HList, A <: HList] private (
  val from: Fragment,
  val select: Fragment
)(
  implicit ae: AliasedBindings[E],
           ca: Composite[A]
) {

  import Selection.Operation
  import Selection.Operation._

  type Can[O <: Operation] = HasOperation[S, O]

  def sql: Fragment = select ++ from

  def distinct(
    implicit ev: Can[Distinct]
  ): Selection[GroupBy, E, A] = {
    void(ev)
    new Selection(from, fr"SELECT DISTINCT")
  }

  def distinctBy[B <: HList, O <: HList](f: AliasedEnv[E] => B)(
    implicit ev: Output.Aux[B, O],
             co: Composite[O]
  ): Selection[GroupBy, E, O] = {
    val cols = f(new AliasedEnv[E])
    val sel  = fr0"SELECT DISTINCT BY(" ++ ev.sql(cols) ++ fr")"
    new Selection(from, sel)
  }

  def groupBy[B <: HList, O <: HList](f: AliasedEnv[E] => B)(
    implicit ev: Output.Aux[B, O],
             co: Composite[O]
  ): Selection[Having, E, O] = {
    val cols = f(new AliasedEnv[E])
    val froʹ = from ++ fr"GROUP BY" ++ ev.sql(cols)
    new Selection(froʹ, select)
  }

  def having(f: AliasedEnv[E] => Expr[Boolean])(
    implicit ev: Can[Having]
  ): Selection[Nothing, E, A] = {
    void(ev)
    val cond = f(new AliasedEnv[E]).sql
    new Selection(from ++ fr"HAVING" ++ cond, select)
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
  }
  import Operation._

  def fromFragment[E <: HList, A <: HList](select: Fragment)(
    implicit ae: AliasedBindings[E],
             ca: Composite[A]
  ): Selection[Distinct with GroupBy, E, A] =
    new Selection(select, fr"SELECT")

}