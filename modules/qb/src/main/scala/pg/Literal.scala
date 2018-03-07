package doobie.labs.qb
package pg

import shapeless.HNil

/**
 * Typeclass witnessing that Scala type A can be encoded as an unascribed SQL literal of type S.
 */
class Literal[A](sql: A => String) {

  type Out <: XString

  def apply(a: A): PgExpr[HNil, HNil, HNil, HNil, Out] =
    new PgExpr[HNil, HNil, HNil, HNil, Out] {
      def sql = Literal.this.sql(a)
      override def psql = sql
    }

}

object Literal {

  private def mk[A, S0 <: XString](f: A => String): Literal[A] { type Out = S0 } =
    new Literal[A](f) { type Out = S0 }

  // Value types
  implicit val boolean_bool  = mk[Boolean, "bool"](_.toString)
  implicit val short_int2    = mk[Short, "int2"](_.toString)
  implicit val int_int4      = mk[Int, "int4"](_.toString)
  implicit val long_int8     = mk[Long, "int8"](_.toString)
  implicit val float_float4  = mk[Float, "float4"](_.toString)
  implicit val double_float8 = mk[Double, "float8"](_.toString)

  // Strings
  implicit val string_varchar = mk[String, "varchar"](s => {
    val escaped = s.replaceAll("'", "''")
    s"'$escaped'"
  })

  // Dates, etc.

}