package doobie.labs.qb

import doobie._, doobie.implicits._

// This needs to be built out to be more typeful.
trait Expr[A] { self =>

  // should not have a trailing space
  def sql: Fragment

  private def untypedComparison(e: Expr[_], op: String): Expr[Boolean] =
    Expr.untypedComparison(this, e, op)

  // Some common comparison operations. All untyped in SQL :shrug:
  def ===(e: Expr[_]) = untypedComparison(e, "=")
  def =/=(e: Expr[_]) = untypedComparison(e, "!=")
  def <  (e: Expr[_]) = untypedComparison(e, "<")
  def <= (e: Expr[_]) = untypedComparison(e, "<=")
  def >  (e: Expr[_]) = untypedComparison(e, ">")
  def >= (e: Expr[_]) = untypedComparison(e, ">=")

  // Boolean comparisons are typed
  def and(e: Expr[Boolean])(
    implicit ev: A =:= Boolean
  ): Expr[Boolean] = { void(ev); untypedComparison(e, "AND") }

  def or(e: Expr[Boolean])(
    implicit ev: A =:= Boolean
  ): Expr[Boolean] = { void(ev); untypedComparison(e, "AND") }

}

object Expr {

  def apply[A: Meta](a: A): Expr[A] =
    new Expr[A] {
      val sql = fr0"$a"
    }

  def untypedComparison(a: Expr[_], b: Expr[_], op: String): Expr[Boolean] =
    new Expr[Boolean] {
      val sql = fr0"(" ++ a.sql ++ Fragment.const(" " + op) ++ b.sql ++ fr")"
    }

  def not(e: Expr[Boolean]): Expr[Boolean] =
    new Expr[Boolean] {
      def sql = fr"(NOT" ++ e.sql ++ fr")"
    }

  def max[A](e: Expr[A]): Expr[A] =
    new Expr[A] {
      def sql = fr0"MAX(" ++ e.sql ++ fr0")"
    }

}