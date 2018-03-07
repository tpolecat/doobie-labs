package doobie.labs.qb
package pg

object Test {

  import func._

  def main(args: Array[String]): Unit = {

    val z = length(?::text) > 42::int4 // length(?::text) > 42


    println(z)
  }


}