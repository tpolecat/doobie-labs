package doobie.labs.qb.pg
package proof

import scala.annotation.implicitNotFound
import shapeless.{ HList, HNil, :: }
import shapeless.ops.hlist.Prepend

/**
 * Witness that an arg list of PgExpr for an aggregate function has the resulting accumulated
 * type argument P, G, N, (there is no U ... it's always HNil) and the list of type names A.
 */
@implicitNotFound("Invalid argument type(s). All must be PgExprs.")
trait ArgListAgg[L <: HList] {
  type P <: HList
  type G <: HList
  type N <: HList
  type A <: HList // an HList of XStrings
}

object ArgListAgg extends ArgListAggLow {

  @implicitNotFound("Invalid argument type(s). All must be PgExprs.")
  type Aux[L <: HList, P0 <: HList, G0 <: HList, N0 <: HList, A0 <: HList] =
    ArgListAgg[L] {
      type P = P0
      type G = G0
      type N = N0
      type A = A0
    }

  implicit def hnil: Aux[HNil, HNil, HNil, HNil, HNil] =
    new ArgListAgg[HNil] {
      type P = HNil
      type G = HNil
      type N = HNil
      type A = HNil
    }

  implicit def hcons[
                P  <: HList, U  <: HList, G  <: HList, N  <: HList, Ax <: XString, // head
    T <: HList, P0 <: HList,              G0 <: HList, N0 <: HList, A0 <: HList,   // tail
    UG <: HList // U ++ G
  ](
    implicit ev: Aux[T, P0, G0, N0, A0],
             pp: Prepend[P, P0],
             pn: Prepend[N, N0],
             ug: Prepend.Aux[U, G, UG],
             pg: Prepend[UG, G0]
  ): Aux[PgExpr[P, U, G, N, Ax] :: T, pp.Out, pg.Out, pn.Out, Ax :: A0] =
     new ArgListAgg[PgExpr[P, U, G, N, Ax] :: T] {
      void(ev, ug)
      type P = pp.Out
      type G = pg.Out
      type N = pn.Out
      type A = Ax :: A0
    }

}

trait ArgListAggLow { this: ArgListAgg.type =>

  // special handling for ColRef
  implicit def hcons2[Nx <: XString, C <: XString, Ax <: XString, T <: HList](
    implicit ev: ArgListAgg[T]
  ): Aux[ColRef[Nx, C, Ax] :: T, ev.P, (Nx, C) :: ev.G, ev.N, Ax :: ev.A] =
     new ArgListAgg[ColRef[Nx, C, Ax] :: T] {
      type P = ev.P
      type G = (Nx, C) :: ev.G
      type N = ev.N
      type A = Ax :: ev.A
    }

}