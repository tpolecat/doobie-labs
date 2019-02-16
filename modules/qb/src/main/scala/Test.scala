package doobie.labs.qb

import cats.implicits._
import cats.effect._
import doobie._
import shapeless._
import Expr.max

object Test extends IOApp {

  // yadda
  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:world",
    "postgres", ""
  )
  val y = xa.yolo; import y._

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

  // Aliases are introduced via Dynamic. Env lookups are also via Dynamic and are checked at compile
  // time (misspell a table/column name and it doesn't compile). Columns types are tracked through
  // to the end. Outer joins lift one side or the other into Option. Expr(a) introduces a parameter
  // and captures the argument. End result is a Query0.
  def query(maxPop: Int) =
    from(country.as.k)
      .leftJoin(city.as.c).on(ε => ε.k.code === ε.c.countrycode)
      .select (ε => ε.k.name :: max(ε.c.population) :: HNil)
      .groupBy(ε => ε.k.name :: HNil)
      .having (ε => max(ε.c.population) < Expr(maxPop))
      .orderBy(ε => max(ε.c.population) :: HNil)
      .done
      .map(_.tupled) // Query0[(String, Option[Int])]

  def run(args: List[String]): IO[ExitCode] = {
    void(args)
    val q = query(1000)
    (q.check *> IO(println) *> q.quick).as(ExitCode.Success)
  }

  // SELECT k.name, MAX(c.population)
  // FROM country AS k
  // LEFT JOIN city AS c ON (k.code = c.countrycode)
  // GROUP BY k.name
  // HAVING (MAX(c.population) < ?)
  // ORDER BY MAX(c.population)
  //
  // ✓ SQL Compiles and Typechecks
  // ✓ P01 Int  →  INTEGER (int4)
  // ✓ C01 name VARCHAR (varchar) NOT NULL  →  String
  // ✓ C02 max  INTEGER (int4)    NULL?     →  Option[Int]
  //
  // (Pitcairn,Some(42))
  // (Tokelau,Some(300))
  // (Holy See (Vatican City State),Some(455))
  // (Cocos (Keeling) Islands,Some(503))
  // (Niue,Some(682))
  // (Christmas Island,Some(700))
  // (Norfolk Island,Some(800))
  // (Anguilla,Some(961))

}
