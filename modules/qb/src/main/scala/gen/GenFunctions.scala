package doobie.labs.qb.gen

// import cats._
import cats.effect._
// import cats.implicits._
import doobie._
import doobie.postgres.implicits._
import doobie.implicits._

final case class Signature(
  argTypes: List[String],
  retType: String,
  modes: Option[List[String]]
) {
  def exists(f: String => Boolean) =
    argTypes.exists(f) || f(retType)
}

final case class Func(
  name: String,
  isAggregate: Boolean,
  sigs: List[Signature]
) {
  def isEmpty = sigs.isEmpty
  def filterNot(f: String => Boolean): Func =
    copy(sigs = sigs.filterNot(_.exists(f)))
  def simpleModes: Func =
    copy(sigs = sigs.filterNot(_.modes.isDefined))
}


object GenFunctions {

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:world",
    "postgres", ""
  )

  def namespace(name: String): ConnectionIO[Int] =
    sql"""
      SELECT oid
      FROM pg_namespace
      WHERE nspname = $name
    """.query[Int].unique

  def types(ns: Int): ConnectionIO[Map[Int, String]] =
    sql"""
      SELECT oid, typname
      FROM pg_type
      WHERE typnamespace = $ns
    """.query[(Int, String)].to[List].map(_.toMap)

  def funcs(ns: Int, ts: Map[Int, String]): ConnectionIO[List[Func]] =
    sql"""
      SELECT proname, proisagg, proargtypes::_int4, prorettype, proargmodes
      FROM pg_proc
      WHERE pronamespace = $ns
    """.query[(String, Boolean, List[Int], Int, Option[List[String]])]
       .to[List]
       .map(_.groupBy(t => (t._1, t._2)).map { case ((n, a), ss) =>
          Func(n, a, ss.map { case (_, _, as, r, ms) =>
            Signature(as.map(ts), ts(r), ms)
          })
       }.toList)

  // internal types
  val internal: Set[String] =
    Set(
      "cstring", "internal", "language_handler", "fdw_handler", "record", "trigger",
      "event_trigger", "opaque"
    )

  // Generic pseudotypes ... not supported yet
  val generic: Set[String] =
    Set("any", "anyelement", "anyarray", "anynonarray", "anyenum", "anyrange")

  val forbidden: Set[String] =
    internal ++
    generic // temporary

  def loadFuncs: ConnectionIO[List[Func]] =
    for {
      ns <- namespace("pg_catalog")
      ts <- types(ns)
      fs <- funcs(ns, ts)
    } yield fs.map(_.filterNot(forbidden).simpleModes)
              .filterNot(_.isEmpty)
              .filterNot(_.name.startsWith("pg_"))
              .sortBy(_.name)

  def ioMain: IO[Unit] =
    for {
      fs <- loadFuncs.transact(xa)
      _  <- IO(fs.foreach(println))
      _  <- IO(println(s"Loaded ${fs.size} user functions."))
    } yield ()

  def main(args: Array[String]): Unit =
    ioMain.unsafeRunSync

}