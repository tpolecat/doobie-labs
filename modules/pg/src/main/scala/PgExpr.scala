package doobie.labs.qb
package pg

import doobie.labs.qb.pg.cast.ImplicitCast
import scala.language.dynamics
import shapeless._
import shapeless.ops.hlist.Prepend

trait PgExpr[
  P <: HList,  // parameter types, a list of xstring
  U <: HList,  // ungrouped columns, a list of pairs of xstring
  G <: HList,  // grouped columns, a list of pairs of xstring
  N <: HList,  // non-null proven columns, a list of pairs of xstring
  A <: XString // schema type, an xstring
] extends Dynamic { outer =>

  /** This expression's SQL source code. */
  def sql: String

  /**
   * This expression's SQL source code, parenthesized when appropriate. For atomic expressions like
   * `42` no parens are added, but for things like `true AND false` they are. This ensures that
   * Scala associativity is mirrored in the generated SQL. Combinators should use this form; user
   * code should use plain `sql`.
   */
  def psql: String = s"($sql)"

  /**
   * Apply a binary operator with the given argument. Binary operators never affect any of the
   * accumulated context, so we simply concatenate the left and right side contexts.
   */
  def applyDynamic[
    O  <: XString,
    Pʹ <: HList,
    Uʹ <: HList,
    Gʹ <: HList,
    Nʹ <: HList,
    B  <: XString,
  ](opname: O)(rhs: PgExpr[Pʹ, Uʹ, Gʹ, Nʹ, B])(
    implicit op: Operator[O, A, B],
             pp: Prepend[P, Pʹ],
             pu: Prepend[U, Uʹ],
             pg: Prepend[G, Gʹ],
             pn: Prepend[N, Nʹ]
  ): PgExpr[pp.Out, pu.Out, pg.Out, pn.Out, op.Out] =
    new PgExpr[pp.Out, pu.Out, pg.Out, pn.Out, op.Out] {
      void(opname)
      def sql = s"${outer.sql} ${op.name} ${rhs.sql}"
    }

  /**
   * Specialized trap for `applyDynamic` calls to `+` which will otherwise be clobbered by
   * any2stringAdd.
   */
  def +[
    Pʹ <: HList,
    Uʹ <: HList,
    Gʹ <: HList,
    Nʹ <: HList,
    B  <: XString,
  ](rhs: PgExpr[Pʹ, Uʹ, Gʹ, Nʹ, B])(
    implicit op: Operator["+", A, B],
             pp: Prepend[P, Pʹ],
             pu: Prepend[U, Uʹ],
             pg: Prepend[G, Gʹ],
             pn: Prepend[N, Nʹ]
  ): PgExpr[pp.Out, pu.Out, pg.Out, pn.Out, op.Out] =
    applyDynamic("+")(rhs)

  override final def toString =
    s"PgExpr($sql)"

}

/** A PgExpr that references a column given as a table+column alias and a Schema type. */
sealed trait ColRef[T <: XString, C <: XString, A <: XString]
  extends PgExpr[HNil, (T, C) :: HNil, HNil, HNil, A] {

  def isNotNull: PgExpr[HNil, (T, C) :: HNil, HNil, (T, C) :: HNil, "bool"] =
    new PgExpr[HNil, (T, C) :: HNil, HNil, (T, C) :: HNil, "bool"] {
      val sql = s"${ColRef.this.psql} IS NOT NULL"
    }

}
object ColRef {

  /** Construct a ColRef. `ColRef["city", "name", "text"]` yields SQL `city.name`. */
  def apply[T <: XString: ValueOf, C <: XString: ValueOf, A <: XString]: ColRef[T, C, A] =
    new ColRef[T, C, A] {
      val sql = s"${valueOf[T]}.${valueOf[C]}"
      override val psql = sql
    }

}

object PgExpr {

  def apply[A, S <: XString](a: A)(
    implicit ev: Literal[A]
  ): PgExpr[HNil, HNil, HNil, HNil, ev.Out] =
    ev(a)

  /** Construct a parameter of the given type. `param["int4"]` yields SQL like `?::int4". */
  def param[U <: XString: ValueOf]: PgExpr[U :: HNil, HNil, HNil, HNil, U] =
    new PgExpr[U :: HNil, HNil, HNil, HNil, U] {
      val sql = s"?::${valueOf[U]}"
      override def psql = sql // no need to parenthesize
    }

  /**
   * We can convert `PgExpr[..., A]` to `PgExpr[..., B]` when there is an `ImplicitCast[A, B]`
   * available, and do so silently. This allows us to pass an `int2` where we need an `int4`, for
   * example. This mimics Postgres behavior.
   */
  implicit def implicitCast[
    P <: HList,
    U <: HList,
    G <: HList,
    N <: HList,
    A <: XString,
    B <: XString
  ](e: PgExpr[P, U, G, N, A])(
    implicit ev: ImplicitCast[A, B]
  ): PgExpr[P, U, G, N, B] =
    ev(e)

  /** Extension methods. */
  implicit class PgExprOps[
    P <: HList,
    U <: HList,
    G <: HList,
    N <: HList,
    A <: XString,
  ](val self: PgExpr[P, U, G, N, A]) {

    /**
     * For ColRef this operation accumulates a new non-null proof, but the general operation
     * on PgExpr does not. This is the "fallback" case which needs to be added with syntax.
     */
    def isNotNull: PgExpr[P, U, G, N, "bool"] =
      new PgExpr[P, U, G, N, "bool"] {
        val sql = s"${self.psql} IS NOT NULL"
      }

  }

}

