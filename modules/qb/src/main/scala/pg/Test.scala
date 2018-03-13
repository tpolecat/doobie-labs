package doobie.labs.qb
package pg


object Test {

  import func._
  import shapeless._

  def main(args: Array[String]): Unit = {

    val z: PgExpr["text" :: HNil, ("city", "population") :: HNil, HNil, HNil, "bool"] =
      length(?::text) > ColRef["city", "population", "int8"] // length(?::text) > city.population

    println(z)
  }


}