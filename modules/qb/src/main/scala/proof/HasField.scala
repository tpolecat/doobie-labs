package doobie.labs.qb
package proof

import scala.annotation._
import shapeless._

@implicitNotFound("Key ${K} not found in environmnent ${L}")
trait HasField[L <: HList, K <: XString] {
  type Out
}

object HasField {

  def apply[L <: HList, K <: XString](implicit ev: HasField[L, K]): ev.type = ev

  @implicitNotFound("Key ${K} not found in environmnent ${L}")
  type Aux[L <: HList, K <: XString, V] = HasField[L, K] { type Out = V }

  implicit def head[K <: XString, V, T <: HList]: HasField.Aux[(K, V) :: T, K, V] =
    new HasField[(K, V) :: T, K] {
      type Out = V
    }

  implicit def tail[H, T <: HList, K <: XString, V](
    implicit ev: HasField.Aux[T, K, V]
  ): HasField.Aux[H :: T, K, V] =
    new HasField[H :: T, K] {
      void(ev)
      type Out = V
    }

}
