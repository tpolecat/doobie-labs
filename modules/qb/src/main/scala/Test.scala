package doobie.labs.qb

// import doobie._, doobbie.imports._
import shapeless._

object Test {

  // Table is defined entirely in the type.
  val city = Table["city",
    ("id",          Int)    ::
    ("name",        String) ::
    ("countrycode", String) ::
    ("district",    String) ::
    ("population",  Int)    :: HNil
  ]

  // SELECT k.name, MAX(c.population) AS maxpop
  // FROM country AS k
  // LEFT JOIN city AS c ON k.code = c.countrycode
  // GROUP BY k.name
  // HAVING max(c.population) < 10000
  // ORDER BY maxpop ASC

  def query(s: String) =
    city.as.c1
      .leftJoin(city.as.c2)
      .on     { ε => ε.c1.name === ε.c2.name }
      .where  { ε => (ε.c1.id =/= ε.c2.id) and (ε.c1.name =/= Expr(s)) }
      .select { ε => ε.c1.id :: ε.c2.name :: Expr(42) :: HNil }
      .distinct
      .groupBy { ε => ε.c1.countrycode :: HNil }
        // can only use fields that don't appear in select, or appear only in aggregate functions
        // we need a new environment here for HAVING
      .having  { ε => ε.c2.id > Expr(10) }       // we can only refer to fields in GROUP BY or used in aggregate

  val x = query("Atlanta") // Query0[Int :: Option[String] :: Int :: HNil]

}
