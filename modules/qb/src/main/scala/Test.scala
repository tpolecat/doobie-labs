package doobie.labs.qb

import doobie.labs.qb.proof._
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

  val country = Table["country",
    ("code",           String)             ::
    ("name",           String)             ::
    ("continent",      String)             ::
    ("region",         String)             ::
    ("surfacearea",    Float)              ::
    ("indepyear",      Option[Short])      ::
    ("population",     Int)                ::
    ("lifeexpectancy", Option[Float])      ::
    ("gnp",            Option[BigDecimal]) ::
    ("gnpold",         Option[BigDecimal]) ::
    ("localname",      String)             ::
    ("governmentform", String)             ::
    ("headofstate",    Option[String])     ::
    ("capital",        Option[Int])        ::
    ("code2",          String)             :: HNil
  ]

  // really we want `HasBinding[E, "c", "population"]
  def maxPop[E <: HList, B <: HList](e: AliasedEnv[E])(
    implicit eh: HasField.Aux[E, "c", B],
             xx: Bindings[B],
             bh: HasField[B, "population"]
  ): Expr[bh.Out] =
    Expr.max(e.c.population)

  import Expr.max

  // SELECT k.name, MAX(c.population)
  // FROM country AS k
  // LEFT JOIN city AS c ON k.code = c.countrycode
  // GROUP BY k.name
  // HAVING MAX(c.population) < 10000
  // ORDER BY MAX(c.population) ASC
  val z =
    from(country.as.k)
      .leftJoin(city.as.c).on(e => e.k.code === e.c.countrycode)
      .select (e => e.k.name :: max(e.c.population) :: HNil)
      .groupBy(e => e.k.name :: HNil)
      .having (e => max(e.c.population) < Expr(10000))
      .orderBy(e => max(e.c.population) :: HNil)

}
