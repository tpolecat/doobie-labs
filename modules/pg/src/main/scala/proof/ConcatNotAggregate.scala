package doobie.labs.qb.pg
package proof

import scala.annotation.implicitNotFound
import shapeless.{ HList, HNil, :: }
import shapeless.ops.hlist.Prepend

@implicitNotFound("Invalid argument type(s). All must be PgExprs.")
trait ConcatNonAggregate[L <: HList] {
  type P <: HList
  type U <: HList
  type G <: HList
  type N <: HList
  type A <: HList // an HList of XStrings
}
object ConcatNonAggregate extends ConcatNonAggregateLow {

@implicitNotFound("Invalid argument type(s). All must be PgExprs.")
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
      void(ev)
      type P = pp.Out
      type U = pu.Out
      type G = pg.Out
      type N = pn.Out
      type A = Ax :: A0
    }

}
trait ConcatNonAggregateLow { this: ConcatNonAggregate.type =>

  implicit def hcons2[Nx <: XString, C <: XString, Ax <: XString, T <: HList](
    implicit ev: ConcatNonAggregate[T]
  ): Aux[ColRef[Nx, C, Ax] :: T, ev.P, (Nx, C) :: ev.U, ev.G, ev.N, Ax :: ev.A] =
     new ConcatNonAggregate[ColRef[Nx, C, Ax] :: T] {
      type P = ev.P
      type U = (Nx, C) :: ev.U
      type G = ev.G
      type N = ev.N
      type A = Ax :: ev.A
    }

}