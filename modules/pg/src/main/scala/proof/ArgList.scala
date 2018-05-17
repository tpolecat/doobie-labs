package doobie.labs.qb.pg
package proof

import scala.annotation.implicitNotFound
import shapeless.{ HList, HNil, :: }
import shapeless.ops.hlist.Prepend

/**
 * Witness that an arg list of PgExpr for a non-aggregate function has the resulting accumulated
 * type argument P, U, G, N, and the list of type names A.
 */
@implicitNotFound("Invalid argument type(s). All must be PgExprs.")
trait ArgList[L <: HList] {
  type P <: HList
  type U <: HList
  type G <: HList
  type N <: HList
  type A <: HList // an HList of XStrings
}

object ArgList extends ArgListLow {

  @implicitNotFound("Invalid argument type(s). All must be PgExprs.")
  type Aux[L <: HList, P0 <: HList, U0 <: HList, G0 <: HList, N0 <: HList, A0 <: HList] =
    ArgList[L] {
      type P = P0
      type U = U0
      type G = G0
      type N = N0
      type A = A0
    }

  implicit def hnil: Aux[HNil, HNil, HNil, HNil, HNil, HNil] =
    new ArgList[HNil] {
      type P = HNil
      type U = HNil
      type G = HNil
      type N = HNil
      type A = HNil
    }

  // prepending a PgExpr just prepends its type arguments pairwise
  implicit def hcons[
                P  <: HList, U  <: HList, G  <: HList, N  <: HList, Ax <: XString, // head
    T <: HList, P0 <: HList, U0 <: HList, G0 <: HList, N0 <: HList, A0 <: HList    // tail
  ](
    implicit ev: Aux[T, P0, U0, G0, N0, A0],
             pp: Prepend[P, P0],
             pu: Prepend[U, U0],
             pg: Prepend[G, G0],
             pn: Prepend[N, N0],
  ): Aux[PgExpr[P, U, G, N, Ax] :: T, pp.Out, pu.Out, pg.Out, pn.Out, Ax :: A0] =
     new ArgList[PgExpr[P, U, G, N, Ax] :: T] {
      void(ev)
      type P = pp.Out
      type U = pu.Out
      type G = pg.Out
      type N = pn.Out
      type A = Ax :: A0
    }

}

trait ArgListLow { this: ArgList.type =>

  // special handling for ColRef
  implicit def hcons2[Nx <: XString, C <: XString, Ax <: XString, T <: HList](
    implicit ev: ArgList[T]
  ): Aux[ColRef[Nx, C, Ax] :: T, ev.P, (Nx, C) :: ev.U, ev.G, ev.N, Ax :: ev.A] =
     new ArgList[ColRef[Nx, C, Ax] :: T] {
      type P = ev.P
      type U = (Nx, C) :: ev.U
      type G = ev.G
      type N = ev.N
      type A = Ax :: ev.A
    }

}