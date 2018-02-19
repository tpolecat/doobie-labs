package doobie.labs.qb

import shapeless._

object Test {

  val city =
    Table["city",
      ("id",          Int)    ::
      ("name",        String) ::
      ("countrycode", String) ::
      ("district",    String) ::
      ("population",  Int)    :: HNil
    ]

  val x =
    city.as.c1
      .leftJoin(city.as.c2).on { ε => ε.c1.name === ε.c2.name }
      // .where { e => e.c1.id =/= e.c2.id }

}