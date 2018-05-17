package doobie.labs.qb.pg
package func

import doobie.labs.qb.pg.proof.ArgList
import scala.annotation.implicitNotFound
import shapeless.{ HList, HNil, ::, ProductArgs }
import shapeless.ops.hlist.ToTraversable

/** @group Simple Functions */
object length extends ProductArgs {

  @implicitNotFound("length: invalid arguments. Valid signatures are \n  (text): int4\n  (bpchar): int4\n  (lseg): float8\n  (path): float8\n  (bytea, name): int4\n  (bit): int4\n  (bytea): int4\n  (tsvector): int4")
  trait Args[A <: HList] {
    type Out <: XString
  }
  object Args {
    implicit val text_int4 = new Args["text" :: HNil] { type Out = "int4" }
    implicit val bpchar_int4 = new Args["bpchar" :: HNil] { type Out = "int4" }
    implicit val lseg_float8 = new Args["lseg" :: HNil] { type Out = "float8" }
    implicit val path_float8 = new Args["path" :: HNil] { type Out = "float8" }
    implicit val bytea_name_int4 = new Args["bytea" :: "name" :: HNil] { type Out = "int4" }
    implicit val bit_int4 = new Args["bit" :: HNil] { type Out = "int4" }
    implicit val bytea_int4 = new Args["bytea" :: HNil] { type Out = "int4" }
    implicit val tsvector_int4 = new Args["tsvector" :: HNil] { type Out = "int4" }
  }

  def applyProduct[L <: HList, P <: HList, U <: HList, G <: HList, N <: HList, A <: HList](args: L)(
    implicit ev: ArgList.Aux[L, P, U, G, N, A],
             as: Args[A],
             tt: ToTraversable.Aux[L, List, PgExpr[_, _, _, _, _]]
  ): PgExpr[P, U, G, N, as.Out] =
    new PgExpr[P, U, G, N, as.Out] {
      void(ev)
      def sql = args.toList.map(_.sql).mkString("length(", ", ", ")")
      override def psql = sql
    }

}
