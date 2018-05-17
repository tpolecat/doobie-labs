package doobie.labs.qb.pg
package func

import doobie.labs.qb.pg.proof.ArgListAgg
import scala.annotation.implicitNotFound
import shapeless.{ HList, HNil, ::, ProductArgs }
import shapeless.ops.hlist.ToTraversable

/** @group Aggregate Functions */
object max extends ProductArgs {

  @implicitNotFound("max: invalid arguments. Valid signatures are \n  (int8): int8\n  (int4): int4\n  (int2): int2\n  (oid): oid\n  (float4): float4\n  (float8): float8\n  (abstime): abstime\n  (date): date\n  (time): time\n  (timetz): timetz\n  (money): money\n  (timestamp): timestamp\n  (timestamptz): timestamptz\n  (interval): interval\n  (text): text\n  (numeric): numeric\n  (bpchar): bpchar\n  (tid): tid\n  (inet): inet")
  trait Args[A <: HList] {
    type Out <: XString
  }
  object Args {
    implicit val int8_int8 = new Args["int8" :: HNil] { type Out = "int8" }
    implicit val int4_int4 = new Args["int4" :: HNil] { type Out = "int4" }
    implicit val int2_int2 = new Args["int2" :: HNil] { type Out = "int2" }
    implicit val oid_oid = new Args["oid" :: HNil] { type Out = "oid" }
    implicit val float4_float4 = new Args["float4" :: HNil] { type Out = "float4" }
    implicit val float8_float8 = new Args["float8" :: HNil] { type Out = "float8" }
    implicit val abstime_abstime = new Args["abstime" :: HNil] { type Out = "abstime" }
    implicit val date_date = new Args["date" :: HNil] { type Out = "date" }
    implicit val time_time = new Args["time" :: HNil] { type Out = "time" }
    implicit val timetz_timetz = new Args["timetz" :: HNil] { type Out = "timetz" }
    implicit val money_money = new Args["money" :: HNil] { type Out = "money" }
    implicit val timestamp_timestamp = new Args["timestamp" :: HNil] { type Out = "timestamp" }
    implicit val timestamptz_timestamptz = new Args["timestamptz" :: HNil] { type Out = "timestamptz" }
    implicit val interval_interval = new Args["interval" :: HNil] { type Out = "interval" }
    implicit val text_text = new Args["text" :: HNil] { type Out = "text" }
    implicit val numeric_numeric = new Args["numeric" :: HNil] { type Out = "numeric" }
    implicit val bpchar_bpchar = new Args["bpchar" :: HNil] { type Out = "bpchar" }
    implicit val tid_tid = new Args["tid" :: HNil] { type Out = "tid" }
    implicit val inet_inet = new Args["inet" :: HNil] { type Out = "inet" }
  }

  def applyProduct[L <: HList, P <: HList, G <: HList, N <: HList, A <: HList](args: L)(
    implicit ev: ArgListAgg.Aux[L, P, G, N, A],
            as: Args[A],
            tt: ToTraversable.Aux[L, List, PgExpr[_, _, _, _, _]]
  ): PgExpr[P, HNil, G, N, as.Out] =
    new PgExpr[P, HNil, G, N, as.Out] {
      void(ev)
      def sql = args.toList.map(_.sql).mkString("max(", ", ", ")")
      override def psql = sql
    }

}
