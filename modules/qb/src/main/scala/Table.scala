package doobie.labs.qb

import cats.implicits._
import doobie.Fragment
import doobie.labs.qb.proof._
import scala.language.dynamics
import shapeless._

/**
 * A table has a name and an alias. The alias is also known at the type level as `A`. The mapping
 * from column name to Scala type is known '''only''' at the type level as `E`, with `Bindings[E]`
 * witnessing that `E` has the proper structure. We over-constrain `E` to catch errors earlier,
 * when they will make more sense to the user.
 */
final class Table[A <: XString, E <: HList] private (name: String, alias: A)(
  implicit b: Bindings[E]
) {

  /** A `Table`'s SQL fragment is the table name, or name `AS` alias. */
  def sql: Fragment =
    Fragment.const(if (name === alias) name else s"$name AS $alias")

  /** We don't want the user dealing with strings, so we alias via `Dynamic` as `table.as.alias`. */
  object as extends Dynamic {
    def selectDynamic(alias: String): Table[alias.type, E] =
      new Table(name, alias)
  }

  override def toString =
    s"Table($sql)"

}

object Table {

  /** Construct a `Table` given compatible types `A` and `E`. */
  def apply[A <: XString, E <: HList](
    implicit be: Bindings[E],
             na: Witness.Aux[A]
  ): Table[A, E] =
    new Table(na.value, na.value)

}
