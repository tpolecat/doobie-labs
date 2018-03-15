package doobie.labs.qb
package pg


object Test {

  import func._
  import shapeless._

  val z: PgExpr["text" :: HNil, ("city", "population") :: HNil, HNil, HNil, "bool"] =
    length(?::text) > ColRef["city", "population", "int8"] // length(?::text) > city.population

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