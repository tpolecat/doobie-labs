package doobie.labs.qb

import doobie.labs.qb.proof._
import shapeless._

final class Where[E <: HList](sql: String)(
  implicit ev: AliasedBindings[E]
) {
  void(ev, sql)

  override def toString =
    s"Where($sql)"

}