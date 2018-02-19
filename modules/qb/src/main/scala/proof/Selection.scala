package doobie.labs.qb
package proof

import doobie._, doobie.implicits._
import shapeless.{ HList, ::, HNil }

// a hlist of expr[a] where we have composite for the inner types.
trait Selection[E <: HList] {
  type Out <: HList
  def sql(e: E): Fragment
}
object Selection {
  def apply[E <: HList](implicit ev: Selection[E]): ev.type = ev

  type Aux[E <: HList, O <: HList] = Selection[E] { type Out = O }

  implicit val hnil: Selection.Aux[HNil, HNil] =
    new Selection[HNil] {
      type Out = HNil
      def sql(e: HNil) = Fragment.empty
    }

  implicit def hcons[H, T <: HList](
    implicit ev: Selection[T]
  ): Selection.Aux[Expr[H] :: T, H :: ev.Out] =
    new Selection[Expr[H] :: T] {
      type Out = H :: ev.Out
      def sql(e: Expr[H] :: T) =
        e match {
          case h :: HNil => h.sql
          case h :: t    => h.sql ++ fr"," ++ ev.sql(t)
        }
    }

}
