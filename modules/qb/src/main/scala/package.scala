package doobie.labs

import doobie.labs.qb.proof._
import shapeless._

package object qb {

  type XString = String with Singleton

  implicit def tableToJoin[A <: XString, E <: HList](t: Table[A, E])(
    implicit ab: AliasedBindings[(A, E) :: HNil]
  ): Join[(A, E) :: HNil] =
    Join.fromTable(t)

  def void(as: Any*): Unit = (as, ())._2

}