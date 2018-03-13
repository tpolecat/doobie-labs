package doobie.labs.qb
package pg
package func

import scala.annotation.implicitNotFound
import shapeless._
import shapeless.ops.hlist._

// define multi-arg functions inductively
trait ConcatNonAggregate[L <: HList] {
  type P <: HList
  type U <: HList
  type G <: HList
  type N <: HList
  type A <: HList // an HList of XStrings
}
object ConcatNonAggregate {

  type Aux[L <: HList, P0 <: HList, U0 <: HList, G0 <: HList, N0 <: HList, A0 <: HList] =
    ConcatNonAggregate[L] {
      type P = P0
      type U = U0
      type G = G0
      type N = N0
      type A = A0
    }

  implicit def hnil: Aux[HNil, HNil, HNil, HNil, HNil, HNil] =
    new ConcatNonAggregate[HNil] {
      type P = HNil
      type U = HNil
      type G = HNil
      type N = HNil
      type A = HNil
    }

  implicit def hcons[
                P  <: HList, U  <: HList, G  <: HList, N  <: HList, Ax  <: XString, // head
    T <: HList, P0 <: HList, U0 <: HList, G0 <: HList, N0 <: HList, A0 <: HList    // tail
  ](
    implicit ev: Aux[T, P0, U0, G0, N0, A0],
             pp: Prepend[P, P0],
             pu: Prepend[U, U0],
             pg: Prepend[G, G0],
             pn: Prepend[N, N0],
  ): Aux[PgExpr[P, U, G, N, Ax] :: T, pp.Out, pu.Out, pg.Out, pn.Out, Ax :: A0] =
     new ConcatNonAggregate[PgExpr[P, U, G, N, Ax] :: T] {
      doobie.labs.qb.void(ev)
      type P = pp.Out
      type U = pu.Out
      type G = pg.Out
      type N = pn.Out
      type A = Ax :: A0
    }

}


object age extends ProductArgs {

  @implicitNotFound("Invalid argument type(s). Valid signatures are\n  (xid): int4\n  (timestamptz, timestamptz): interval\n  (timestamptz): interval\n  ...")
  trait Args[A <: HList] {
    type Out <: XString // return type
  }
  object Args {
    implicit val xid_int4 = new Args["xid" :: HNil] { type Out = "int4" }
    implicit val timestamptz_timestamptz_interval = new Args["timestamptz" :: "timestamptz" :: HNil] { type Out = "interval" }
    implicit val timestamptz_interval = new Args["timestamptz" :: HNil] { type Out = "interval" }
    implicit val timestamp_timestamp = new Args["timestamp" :: "timestamp" :: HNil] { type Out = "interval" }
    implicit val timestamp_interval = new Args["timestamp" :: HNil] { type Out = "interval" }
  }

  def applyProduct[L <: HList, P <: HList, U <: HList, G <: HList, N <: HList, A <: HList](args: L)(
    implicit ev: ConcatNonAggregate.Aux[L, P, U, G, N, A],
             as: Args[A],
             tt: ToTraversable.Aux[L, List, PgExpr[_, _, _, _, _]]
  ): PgExpr[P, U, G, N, as.Out] =
    new PgExpr[P, U, G, N, as.Out] {
      doobie.labs.qb.void(ev)
      def sql = args.toList.map(_.sql).mkString("age(", ", ", ")")
    }

}

object agetest {

  val z = age(?::timestamptz, ?::timestamptz) // doh, can't pass a colref because the type is too precise

  val zz: PgExpr["timestamptz" :: "timestamptz" :: HNil, HNil, HNil, HNil, "interval"] = z

}

