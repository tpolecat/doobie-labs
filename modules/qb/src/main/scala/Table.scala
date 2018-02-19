package doobie.labs.qb

import cats.implicits._
import doobie.labs.qb.proof._
import scala.language.dynamics
import shapeless._

final class Table[A <: XString, E <: HList] private (name: String, alias: A)(
  implicit b: Bindings[E] // unused, just to ease the implementatio
) {

  def sql: String =
    if (name === alias) name else s"$name AS $alias"

  def withAlias(alias: String): Table[alias.type, E] =
    new Table(name, alias)

  object as extends Dynamic {
    def selectDynamic(alias: String): Table[alias.type, E] =
      withAlias(alias)
  }

  override def toString =
    s"Table($sql)"

}

object Table {

  def apply[A <: XString, E <: HList](
    implicit be: Bindings[E],
             na: Witness.Aux[A]
  ): Table[A, E] =
    new Table(na.value, na.value)

}
