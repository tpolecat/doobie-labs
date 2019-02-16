package doobie.labs.qb.pg
package gen

// import cats._
import cats.effect._
import cats.implicits._
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

  def arg_instances: String =
    sigs.map { s =>
      s"implicit val ${s.argTypes.mkString("_")}_${s.retType} = " ++
      s"""new Args[${s.argTypes.mkString("\"", "\" :: \"", "\" :: HNil")}] { type Out = "${s.retType}" }"""
    } .mkString("", "\n    ", "")

  def sigHelp: String =
    sigs.map { s =>
      s.argTypes.mkString("(", ", ", s"): ${s.retType}")
    } .mkString("\\n  ", "\\n  ", "")

  override def toString = {
    val tc = if (isAggregate) "ArgListAgg" else "ArgList"

    val apply =
      if (isAggregate)
        s"""|  def applyProduct[L <: HList, P <: HList, G <: HList, N <: HList, A <: HList](args: L)(
            |    implicit ev: ArgListAgg.Aux[L, P, G, N, A],
            |            as: Args[A],
            |            tt: ToTraversable.Aux[L, List, PgExpr[_, _, _, _, _]]
            |  ): PgExpr[P, HNil, G, N, as.Out] =
            |    new PgExpr[P, HNil, G, N, as.Out] {
            |      void(ev)
            |      def sql = args.toList.map(_.sql).mkString("max(", ", ", ")")
            |      override def psql = sql
            |    }
            |""".stripMargin
      else
        s"""|  def applyProduct[L <: HList, P <: HList, U <: HList, G <: HList, N <: HList, A <: HList](args: L)(
            |    implicit ev: ${tc}.Aux[L, P, U, G, N, A],
            |             as: Args[A],
            |             tt: ToTraversable.Aux[L, List, PgExpr[_, _, _, _, _]]
            |  ): PgExpr[P, U, G, N, as.Out] =
            |    new PgExpr[P, U, G, N, as.Out] {
            |      void(ev)
            |      def sql = args.toList.map(_.sql).mkString("$name(", ", ", ")")
            |      override def psql = sql
            |    }
            |""".stripMargin

    s"""|package doobie.labs.qb.pg
        |package func
        |
        |import doobie.labs.qb.pg.proof.${tc}
        |import scala.annotation.implicitNotFound
        |import shapeless.{ HList, HNil, ::, ProductArgs }
        |import shapeless.ops.hlist.ToTraversable
        |
        |/** @group ${if (isAggregate) "Aggregate" else "Simple"} Functions */
        |object $name extends ProductArgs {
        |
        |  @implicitNotFound("$name: invalid arguments. Valid signatures are $sigHelp")
        |  trait Args[A <: HList] {
        |    type Out <: XString
        |  }
        |  object Args {
        |    ${arg_instances}
        |  }
        |
        |$apply
        |}
        |""".stripMargin
  }

}


object GenFunctions extends IOApp {

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
      AND prorows = 0 -- no set-returning functions yet
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
              .sortBy(_.name)
              .filter(f => f.name === "length" ||
                           f.name === "age"    ||
                           f.name === "max")

  def save(f: Func): IO[Unit] =
    IO {
      import better.files._
      val file = file"modules/pg/src/main/scala/func/${f.name}.scala"
      void(file.overwrite(f.toString)(charset = "UTF-8"))
    }

  def run(args: List[String]): IO[ExitCode] =
    for {
      fs <- loadFuncs.transact(xa)
      _  <- fs.traverse(save)
    } yield ExitCode.Success

}