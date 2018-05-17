package doobie.labs.qb.pg
package cast

import scala.annotation.implicitNotFound
import shapeless.HList

@implicitNotFound("${S} cannot be cast to ${T}.")
trait ExplicitCast[S <: String with Singleton, T <: String with Singleton] {
  val source: S
  val target: T
  def apply[
    P <: HList,
    U <: HList,
    G <: HList,
    N <: HList
  ](e: PgExpr[P, U, G, N, S]): PgExpr[P, U, G, N, T] =
    new PgExpr[P, U, G, N, T] {
      def sql = e.psql + "::" ++ target
      override def psql = sql // :: binds tightly
    }
}

object ExplicitCast extends ExplicitCastInstances {

  def apply[S <: String with Singleton, T <: String with Singleton](s: S, t: T): ExplicitCast[S, T] =
    new ExplicitCast[S, T] {
      val source = s
      val target = t
    }

}

trait ExplicitCastInstances extends AssignmentCastInstances {

  implicit val abstime_int4          = ExplicitCast("abstime"  , "int4"    )
  implicit val bit_int4              = ExplicitCast("bit"      , "int4"    )
  implicit val bit_int8              = ExplicitCast("bit"      , "int8"    )
  implicit val bool_int4             = ExplicitCast("bool"     , "int4"    )
  implicit val box_circle            = ExplicitCast("box"      , "circle"  )
  implicit val box_lseg              = ExplicitCast("box"      , "lseg"    )
  implicit val box_point             = ExplicitCast("box"      , "point"   )
  implicit val bpchar_xml            = ExplicitCast("bpchar"   , "xml"     )
  implicit val char_int4             = ExplicitCast("char"     , "int4"    )
  implicit val circle_box            = ExplicitCast("circle"   , "box"     )
  implicit val circle_point          = ExplicitCast("circle"   , "point"   )
  implicit val circle_polygon        = ExplicitCast("circle"   , "polygon" )
  implicit val geography_geometry    = ExplicitCast("geography", "geometry")
  implicit val geometry_path         = ExplicitCast("geometry" , "path"    )
  implicit val geometry_point        = ExplicitCast("geometry" , "point"   )
  implicit val geometry_polygon      = ExplicitCast("geometry" , "polygon" )
  implicit val hstore_json           = ExplicitCast("hstore"   , "json"    )
  implicit val hstore_jsonb          = ExplicitCast("hstore"   , "jsonb"   )
  implicit val int4_abstime          = ExplicitCast("int4"     , "abstime" )
  implicit val int4_bit              = ExplicitCast("int4"     , "bit"     )
  implicit val int4_bool             = ExplicitCast("int4"     , "bool"    )
  implicit val int4_char             = ExplicitCast("int4"     , "char"    )
  implicit val int4_reltime          = ExplicitCast("int4"     , "reltime" )
  implicit val int8_bit              = ExplicitCast("int8"     , "bit"     )
  implicit val lseg_point            = ExplicitCast("lseg"     , "point"   )
  implicit val path_geometry         = ExplicitCast("path"     , "geometry")
  implicit val path_point            = ExplicitCast("path"     , "point"   )
  implicit val point_geometry        = ExplicitCast("point"    , "geometry")
  implicit val polygon_box           = ExplicitCast("polygon"  , "box"     )
  implicit val polygon_circle        = ExplicitCast("polygon"  , "circle"  )
  implicit val polygon_geometry      = ExplicitCast("polygon"  , "geometry")
  implicit val polygon_point         = ExplicitCast("polygon"  , "point"   )
  implicit val reltime_int4          = ExplicitCast("reltime"  , "int4"    )
  implicit val text_xml              = ExplicitCast("text"     , "xml"     )
  implicit val varchar_xml           = ExplicitCast("varchar"  , "xml"     )
  implicit val _text_hstore          = ExplicitCast("_text"    , "hstore"  )

}
