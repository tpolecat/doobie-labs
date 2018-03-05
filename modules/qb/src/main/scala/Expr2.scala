package doobie.labs.qb

import doobie._, doobie.implicits._
import doobie.labs.qb.proof._
import shapeless._, shapeless.ops.hlist.Prepend

// IS NOT NULL can appear in JOIN and WHERE so we need to track it from the beginning.

// We need to track grouped and ungrouped columns in the select. Aggregate functions can't appear
// in JOIN or WHERE clauses so they're first introduced in SELECT. If there are (a) grouped
// columns and (b) any non-grouped columns then there *must* be a GROUP BY clause that mentions
// every non-grouped column. We can set GROUP BY as the only legal operation and require the
// returned HList to discharge all the ungrouped columns. The HAVING clause, if any, can *only*
// reference grouped columns outside of aggregate functions, as with SELECT.

// So we need Expr[
//  U <: HList,      // ungrouped bindings ("c", "foo") :: ... :: HNil
//  G <: Hlist,      // grouped bindings
//  N <: HList,      // columns proven NOT NULL ("c", "bar") :: ... :: HNil
//  A                // scala type
// ]

// For not-null tracking we need to distinguish column references from other exprs. A kind of
// sleazy but probably ok way to do this is to have a column ref subclass with an .isNotNull method
// and then add the general one with syntax.
//
// trait Expr[...]
// trait ColRef[...] { def isNotNull: ... } // e.table.col returns a ColRef ... ok?

// How do we encode U, G, and N in a way that lets us patch the environment? I think we can and it
// will be awesome.

// Beyond this how much do we care about the `A` in terms of allowed operations? SQL is a little
// more loosely typed. We don't care about Option because anything can be null. In fact we might
// want to lower everything in the binding environment. We can conflate numeric types together
// and otherwise try to be typeful and see how it goes. Worst case we will make it impossible to
// express something that's legal but sketchy.



sealed trait Expr2[U <: HList, G <: HList, N <: HList, A] {
  def sql: Fragment
}

sealed trait ColRef[T <: XString, C <: XString, A]
  extends Expr2[(T, C) :: HNil, HNil, HNil, A] { self =>

  /**
   * An expresion <column> IS NOT NULL proves that the column will never be null. This is how non-
   * null proofs are introduced. They are then propagated as the expression grows.
   */
  def isNotNull: Expr2[(T, C) :: HNil, HNil, (T, C) :: HNil, Boolean] =
    new Expr2[(T, C) :: HNil, HNil, (T, C) :: HNil, Boolean] {
      val sql = fr0"(" ++ self.sql ++ fr0"IS NOT NULL)"
    }

}


object Expr2 {

  implicit class Expr2Ops[U <: HList, G <: HList, N <: HList, A](self: Expr2[U, G, N, A]) {

    /**
     * An arbitrary expression <expr> IS NOT NULL doesn't add anything to our knowledge about the
     * columns in play.
     */
    def isNotNull: Expr2[U, G, N, Boolean] =
      new Expr2[U, G, N, Boolean] {
        val sql = fr0"(" ++ self.sql ++ fr0" IS NOT NULL)"
      }

  }

  implicit class Expr2BooleanOps[U <: HList, G <: HList, N <: HList](self: Expr2[U, G, N, Boolean]) {

    /**
     * Conjoining expressions <a> AND <b> yields a new expression with the unions of ungrouped,
     * grouped, and proven not-null columns.
     */
    def and[Uʹ <: HList, Gʹ <: HList, Nʹ <: HList](that: Expr2[Uʹ, Gʹ, Nʹ, Boolean])(
      up: Prepend[U, Uʹ],
      ug: Prepend[G, Gʹ],
      un: Prepend[N, Nʹ]
    ): Expr2[up.Out, ug.Out, un.Out, Boolean] = {
      void(up, ug, un)
      new Expr2[up.Out, ug.Out, un.Out, Boolean] {
        val sql = fr0"(" ++ self.sql ++ fr" AND" ++ that.sql ++ fr0")"
      }
    }

    /**
     * Conjoining expressions <a> AND <b> yields a new expression with the unions of ungrouped
     * and grouped columns, and the intersection of proven not-null columns.
     */
    def or[Uʹ <: HList, Gʹ <: HList, Nʹ <: HList](that: Expr2[Uʹ, Gʹ, Nʹ, Boolean])(
      implicit up: Prepend[U, Uʹ],
               ug: Prepend[G, Gʹ],
               nn: HSet.Intersect[N, Nʹ]
    ): Expr2[up.Out, ug.Out, nn.Out, Boolean] = {
      void(up, ug)
      new Expr2[up.Out, ug.Out, nn.Out, Boolean] {
        val sql = fr0"(" ++ self.sql ++ fr" OR" ++ that.sql ++ fr0")"
      }
    }

  }

}

object Expr2Test {

  def a: Expr2["a" :: HNil, "b" :: HNil, "c" :: HNil, Boolean] = ???
  def b: Expr2["z" :: HNil, HNil, "x" :: "y" :: "c" :: HNil, Boolean] = ???

  def c = a or b

  def d = c : Expr2[
    "a" :: "z" :: HNil,
    "b" :: HNil,
    "c" :: HNil,
    Boolean
  ]

  def cityName: ColRef["city", "name", String] = ???

  def f = cityName.isNotNull: Expr2[("city", "name") :: HNil, HNil, ("city", "name") :: HNil, Boolean]


}