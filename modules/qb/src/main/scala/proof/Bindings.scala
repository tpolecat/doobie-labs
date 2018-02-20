package doobie.labs.qb
package proof

import scala.annotation._
import shapeless._

// witness that E is like ("id", Int) :: ... :: HNil
// where the keys are unique
@implicitNotFound("Invalid Bindings. Expected (\"colname\", Type) :: ... :: HNil; found ${E}")
sealed trait Bindings[E <: HList] {
  type Out <: HList // like E but all types are options
}
object Bindings {

  type Aux[E <: HList, L <: HList] = Bindings[E] { type Out = L }

  def apply[E <: HList](implicit ev: Bindings[E]): ev.type = ev

  implicit val hnil: Bindings.Aux[HNil, HNil] =
    new Bindings[HNil] {
      type Out = HNil
    }

  // TODO: disallow duplicate keys!
  implicit def hcons[K <: XString, V, T <: HList](
    implicit op: V <:< Option[A] forSome { type A },
             bt: Bindings[T],
  ): Bindings.Aux[(K, V) :: T, (K, V) :: bt.Out] =
    new Bindings[(K, V) :: T] {
      void(op)
      type Out = (K, V) :: bt.Out
    }

  implicit def hcons2[K <: XString, V, T <: HList](
    implicit op: V <:!< Option[A] forSome { type A },
             bt: Bindings[T],
  ): Bindings.Aux[(K, V) :: T, (K, Option[V]) :: bt.Out] =
    new Bindings[(K, V) :: T] {
      void(op)
      type Out = (K, Option[V]) :: bt.Out
    }

}

object BindingsTest {

  val foo =
    Bindings[("a", Int) :: ("b", Option[String]) :: HNil]

}

