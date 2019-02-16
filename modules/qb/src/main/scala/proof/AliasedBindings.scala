package doobie.labs.qb
package proof

import scala.annotation._
import shapeless._

// witness that E is like ("table", ("id", Int) :: ... :: HNil) :: ... :: HNil
// where the keys are unique
@implicitNotFound("Invalid NamedBindings. Expected (\"tableAlias\", (\"colname\", Type) :: ... :: HNil) :: ... :: HNil; found ${E}")
sealed trait AliasedBindings[E <: HList]
object AliasedBindings {

  def apply[E <: HList](implicit ev: AliasedBindings[E]): ev.type = ev

  implicit val hnil: AliasedBindings[HNil] =
    new AliasedBindings[HNil] {}

  // TODO: disallow duplicate keys!
  implicit def hcons[A <: XString, H <: HList, T <: HList](
    implicit bh: Bindings[H],
             at: AliasedBindings[T]
  ): AliasedBindings[(A, H) :: T] = {
    void(bh, at)
    new AliasedBindings[(A, H) :: T] {}
  }

}

object AliasedBindingsTest {

  val foo: Any =
    AliasedBindings[("city", ("a", Int) :: ("b", String) :: HNil) :: HNil]

}

