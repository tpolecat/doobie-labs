package doobie.labs.qb
package pg

object Test {

  import func._
  import shapeless._

  val col = ColRef["city", "population", "int8"]

  val z: PgExpr[
    "text" :: HNil,
    ("city", "population") :: HNil,
    ("city", "population") :: HNil,
    HNil, "bool"
  ] =
    length(?::text) > (col + max(col)) // length(?::text) > (city.population + max(city.population))

  /*

  We want the toString to look like:

    (text) -> bool =
      length(?::text) > (city.population + max(city.population)

    Ungrouped: city.population
    Grouped:   city.population
    Non-null:

   */

  val zz =
    age(?::timestamptz, ColRef["foo", "bar", "timestamptz"]) * (?::float8)

  val zzz: PgExpr[
    "timestamptz" :: "float8" :: HNil,
    ("foo", "bar") :: HNil,
    HNil,
    HNil,
    "interval"
  ] = zz

}