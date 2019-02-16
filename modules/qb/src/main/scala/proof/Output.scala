package doobie.labs.qb
package proof

import doobie._, doobie.implicits._
import shapeless.{ HList, ::, HNil }

// a hlist of expr[a] where we have composite for the inner types.
trait Output[E <: HList] {
  type Out <: HList
  def sql(e: E): Fragment
}
object Output {
  def apply[E <: HList](implicit ev: Output[E]): ev.type = ev

  type Aux[E <: HList, O <: HList] = Output[E] { type Out = O }

  implicit val hnil: Output.Aux[HNil, HNil] =
    new Output[HNil] {
      type Out = HNil
      def sql(e: HNil) = Fragment.empty
    }

  implicit def hcons[H, T <: HList](
    implicit ev: Output[T]
  ): Output.Aux[Expr[H] :: T, H :: ev.Out] =
    new Output[Expr[H] :: T] {
      type Out = H :: ev.Out
      def sql(e: Expr[H] :: T) =
        e match {
          case h :: HNil => h.sql
          case h :: t    => h.sql ++ fr"," ++ ev.sql(t)
        }
    }

  implicit def single[H](
  ): Output.Aux[Expr[H] :: HNil, H :: HNil] =
    new Output[Expr[H] :: HNil] {
      type Out = H :: HNil
      def sql(e: Expr[H] :: HNil) =
        e match {
          case h :: HNil => h.sql
        }
    }

}
