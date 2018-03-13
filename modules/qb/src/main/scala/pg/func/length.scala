package doobie.labs.qb
package pg
package func

import scala.annotation.implicitNotFound
import shapeless.HList

// We can encode overloaded methods as top-level objects that we can import one by one and generate
// from system tables. The function-specific Arg type allows us to have a nice, very specific
// implicit not found message.
object length {

  @implicitNotFound("length: expected \"bit\", \"bpchar\", \"bytea\", \"lseg\", \"path\", \"text\", or \"tsvector\";\n        found ${A}")
  class Args[A <: XString] {
    type Out <: XString
    def sql(a: PgExpr[_ <: HList, _ <: HList, _ <: HList, _ <: HList, A]): String = a.sql // by default
  }
  object Args extends ArgsLow {
    type Aux[A <: XString, O] = Args[A] { type Out = O }
    implicit val bit_int4: Aux["bit", "int4"] = new Args["bit"] { type Out = "int4" }
    implicit val bpchar_int4: Aux["bpchar", "int4"] = new Args["bpchar"] { type Out = "int4" }
    implicit val bytea_int4: Aux["bytea", "int4"] = new Args["bytea"] { type Out = "int4" }
    implicit val lseg_float8: Aux["lseg", "float8"] = new Args["lseg"] { type Out = "float8" }
    implicit val path_float8: Aux["path", "float8"] = new Args["path"] { type Out = "float8" }
    implicit val text_int4: Aux["text", "int4"] = new Args["text"] { type Out = "int4" }
    implicit val tsvector_int4: Aux["tsvector", "int4"] = new Args["tsvector"] { type Out = "int4" }
  }
  trait ArgsLow {
    implicit def implicitCast[A <: XString, B <: XString](
      implicit ca: ImplicitCast[A, B],
               ar: Args[B]
    ): Args.Aux[A, ar.Out] =
      new Args[A] {
        type Out = ar.Out
        override def sql(a: PgExpr[_ <: HList, _ <: HList, _ <: HList, _ <: HList, A]) =
          ca(a).sql
      }
  }

  def apply[
    P <: HList,
    U <: HList,
    G <: HList,
    N <: HList,
    A <: XString
  ](a: PgExpr[P, U, G, N, A])(
    implicit ev: Args[A]
  ): PgExpr[P, U, G, N, ev.Out] =
    new PgExpr[P, U, G, N, ev.Out] {
      def sql = s"length(${ev.sql(a)})"
    }

}

