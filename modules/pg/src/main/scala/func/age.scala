package doobie.labs.qb.pg
package func

import doobie.labs.qb.pg.proof.ArgList
import scala.annotation.implicitNotFound
import shapeless.{ HList, HNil, ::, ProductArgs }
import shapeless.ops.hlist.ToTraversable

/** @group Simple Functions */
object age extends ProductArgs {

  @implicitNotFound("age: invalid arguments. Valid signatures are \n  (xid): int4\n  (timestamptz, timestamptz): interval\n  (timestamptz): interval\n  (timestamp, timestamp): interval\n  (timestamp): interval")
  trait Args[A <: HList] {
    type Out <: XString
  }
  object Args {
    implicit val xid_int4 = new Args["xid" :: HNil] { type Out = "int4" }
    implicit val timestamptz_timestamptz_interval = new Args["timestamptz" :: "timestamptz" :: HNil] { type Out = "interval" }
    implicit val timestamptz_interval = new Args["timestamptz" :: HNil] { type Out = "interval" }
    implicit val timestamp_timestamp_interval = new Args["timestamp" :: "timestamp" :: HNil] { type Out = "interval" }
    implicit val timestamp_interval = new Args["timestamp" :: HNil] { type Out = "interval" }
  }

  def applyProduct[L <: HList, P <: HList, U <: HList, G <: HList, N <: HList, A <: HList](args: L)(
    implicit ev: ArgList.Aux[L, P, U, G, N, A],
             as: Args[A],
             tt: ToTraversable.Aux[L, List, PgExpr[_, _, _, _, _]]
  ): PgExpr[P, U, G, N, as.Out] =
    new PgExpr[P, U, G, N, as.Out] {
      void(ev)
      def sql = args.toList.map(_.sql).mkString("age(", ", ", ")")
      override def psql = sql
    }

}
