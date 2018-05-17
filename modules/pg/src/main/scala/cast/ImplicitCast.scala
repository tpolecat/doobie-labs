package doobie.labs.qb.pg
package cast

import shapeless.HList

trait ImplicitCast[S <: String with Singleton, T <: String with Singleton]
  extends AssignmentCast[S, T] {

  override def apply[
    P <: HList,
    U <: HList,
    G <: HList,
    N <: HList
  ](e: PgExpr[P, U, G, N, S]): PgExpr[P, U, G, N, T] =
    new PgExpr[P, U, G, N, T] {
      def sql = e.psql // no rewrite necessary
    }

  }

object ImplicitCast extends ImplicitCastInstances {

  def apply[S <: String with Singleton, T <: String with Singleton](s: S, t: T): ImplicitCast[S, T] =
    new ImplicitCast[S, T] {
      val source = s
      val target = t
    }

}

trait ImplicitCastInstances {

  implicit def id[A <: String with Singleton: ValueOf]: ImplicitCast[A, A] =
    ImplicitCast(valueOf[A], valueOf[A])

  implicit val abstime_timestamp     = ImplicitCast("abstime"      , "timestamp"    )
  implicit val abstime_timestamptz   = ImplicitCast("abstime"      , "timestamptz"  )
  implicit val bit_varbit            = ImplicitCast("bit"          , "varbit"       )
  implicit val box2d_box3d           = ImplicitCast("box2d"        , "box3d"        )
  implicit val box2d_geometry        = ImplicitCast("box2d"        , "geometry"     )
  implicit val box3d_box             = ImplicitCast("box3d"        , "box"          )
  implicit val box3d_box2d           = ImplicitCast("box3d"        , "box2d"        )
  implicit val box3d_geometry        = ImplicitCast("box3d"        , "geometry"     )
  implicit val bpchar_name           = ImplicitCast("bpchar"       , "name"         )
  implicit val bpchar_text           = ImplicitCast("bpchar"       , "text"         )
  implicit val bpchar_varchar        = ImplicitCast("bpchar"       , "varchar"      )
  implicit val bytea_geography       = ImplicitCast("bytea"        , "geography"    )
  implicit val bytea_geometry        = ImplicitCast("bytea"        , "geometry"     )
  implicit val char_text             = ImplicitCast("char"         , "text"         )
  implicit val cidr_inet             = ImplicitCast("cidr"         , "inet"         )
  implicit val citext_text           = ImplicitCast("citext"       , "text"         )
  implicit val citext_varchar        = ImplicitCast("citext"       , "varchar"      )
  implicit val date_timestamp        = ImplicitCast("date"         , "timestamp"    )
  implicit val date_timestamptz      = ImplicitCast("date"         , "timestamptz"  )
  implicit val float4_float8         = ImplicitCast("float4"       , "float8"       )
  implicit val geography_bytea       = ImplicitCast("geography"    , "bytea"        )
  implicit val geometry_box2d        = ImplicitCast("geometry"     , "box2d"        )
  implicit val geometry_box3d        = ImplicitCast("geometry"     , "box3d"        )
  implicit val geometry_bytea        = ImplicitCast("geometry"     , "bytea"        )
  implicit val geometry_geography    = ImplicitCast("geometry"     , "geography"    )
  implicit val geometry_text         = ImplicitCast("geometry"     , "text"         )
  implicit val int2_float4           = ImplicitCast("int2"         , "float4"       )
  implicit val int2_float8           = ImplicitCast("int2"         , "float8"       )
  implicit val int2_int4             = ImplicitCast("int2"         , "int4"         )
  implicit val int2_int8             = ImplicitCast("int2"         , "int8"         )
  implicit val int2_numeric          = ImplicitCast("int2"         , "numeric"      )
  implicit val int2_oid              = ImplicitCast("int2"         , "oid"          )
  implicit val int2_regclass         = ImplicitCast("int2"         , "regclass"     )
  implicit val int2_regconfig        = ImplicitCast("int2"         , "regconfig"    )
  implicit val int2_regdictionary    = ImplicitCast("int2"         , "regdictionary")
  implicit val int2_regnamespace     = ImplicitCast("int2"         , "regnamespace" )
  implicit val int2_regoper          = ImplicitCast("int2"         , "regoper"      )
  implicit val int2_regoperator      = ImplicitCast("int2"         , "regoperator"  )
  implicit val int2_regproc          = ImplicitCast("int2"         , "regproc"      )
  implicit val int2_regprocedure     = ImplicitCast("int2"         , "regprocedure" )
  implicit val int2_regrole          = ImplicitCast("int2"         , "regrole"      )
  implicit val int2_regtype          = ImplicitCast("int2"         , "regtype"      )
  implicit val int4_float4           = ImplicitCast("int4"         , "float4"       )
  implicit val int4_float8           = ImplicitCast("int4"         , "float8"       )
  implicit val int4_int8             = ImplicitCast("int4"         , "int8"         )
  implicit val int4_numeric          = ImplicitCast("int4"         , "numeric"      )
  implicit val int4_oid              = ImplicitCast("int4"         , "oid"          )
  implicit val int4_regclass         = ImplicitCast("int4"         , "regclass"     )
  implicit val int4_regconfig        = ImplicitCast("int4"         , "regconfig"    )
  implicit val int4_regdictionary    = ImplicitCast("int4"         , "regdictionary")
  implicit val int4_regnamespace     = ImplicitCast("int4"         , "regnamespace" )
  implicit val int4_regoper          = ImplicitCast("int4"         , "regoper"      )
  implicit val int4_regoperator      = ImplicitCast("int4"         , "regoperator"  )
  implicit val int4_regproc          = ImplicitCast("int4"         , "regproc"      )
  implicit val int4_regprocedure     = ImplicitCast("int4"         , "regprocedure" )
  implicit val int4_regrole          = ImplicitCast("int4"         , "regrole"      )
  implicit val int4_regtype          = ImplicitCast("int4"         , "regtype"      )
  implicit val int8_float4           = ImplicitCast("int8"         , "float4"       )
  implicit val int8_float8           = ImplicitCast("int8"         , "float8"       )
  implicit val int8_numeric          = ImplicitCast("int8"         , "numeric"      )
  implicit val int8_oid              = ImplicitCast("int8"         , "oid"          )
  implicit val int8_regclass         = ImplicitCast("int8"         , "regclass"     )
  implicit val int8_regconfig        = ImplicitCast("int8"         , "regconfig"    )
  implicit val int8_regdictionary    = ImplicitCast("int8"         , "regdictionary")
  implicit val int8_regnamespace     = ImplicitCast("int8"         , "regnamespace" )
  implicit val int8_regoper          = ImplicitCast("int8"         , "regoper"      )
  implicit val int8_regoperator      = ImplicitCast("int8"         , "regoperator"  )
  implicit val int8_regproc          = ImplicitCast("int8"         , "regproc"      )
  implicit val int8_regprocedure     = ImplicitCast("int8"         , "regprocedure" )
  implicit val int8_regrole          = ImplicitCast("int8"         , "regrole"      )
  implicit val int8_regtype          = ImplicitCast("int8"         , "regtype"      )
  implicit val name_text             = ImplicitCast("name"         , "text"         )
  implicit val numeric_float4        = ImplicitCast("numeric"      , "float4"       )
  implicit val numeric_float8        = ImplicitCast("numeric"      , "float8"       )
  implicit val oid_regclass          = ImplicitCast("oid"          , "regclass"     )
  implicit val oid_regconfig         = ImplicitCast("oid"          , "regconfig"    )
  implicit val oid_regdictionary     = ImplicitCast("oid"          , "regdictionary")
  implicit val oid_regnamespace      = ImplicitCast("oid"          , "regnamespace" )
  implicit val oid_regoper           = ImplicitCast("oid"          , "regoper"      )
  implicit val oid_regoperator       = ImplicitCast("oid"          , "regoperator"  )
  implicit val oid_regproc           = ImplicitCast("oid"          , "regproc"      )
  implicit val oid_regprocedure      = ImplicitCast("oid"          , "regprocedure" )
  implicit val oid_regrole           = ImplicitCast("oid"          , "regrole"      )
  implicit val oid_regtype           = ImplicitCast("oid"          , "regtype"      )
  implicit val pg_node_tree_text     = ImplicitCast("pg_node_tree" , "text"         )
  implicit val regclass_oid          = ImplicitCast("regclass"     , "oid"          )
  implicit val regconfig_oid         = ImplicitCast("regconfig"    , "oid"          )
  implicit val regdictionary_oid     = ImplicitCast("regdictionary", "oid"          )
  implicit val regnamespace_oid      = ImplicitCast("regnamespace" , "oid"          )
  implicit val regoper_oid           = ImplicitCast("regoper"      , "oid"          )
  implicit val regoper_regoperator   = ImplicitCast("regoper"      , "regoperator"  )
  implicit val regoperator_oid       = ImplicitCast("regoperator"  , "oid"          )
  implicit val regoperator_regoper   = ImplicitCast("regoperator"  , "regoper"      )
  implicit val regproc_oid           = ImplicitCast("regproc"      , "oid"          )
  implicit val regproc_regprocedure  = ImplicitCast("regproc"      , "regprocedure" )
  implicit val regprocedure_oid      = ImplicitCast("regprocedure" , "oid"          )
  implicit val regprocedure_regproc  = ImplicitCast("regprocedure" , "regproc"      )
  implicit val regrole_oid           = ImplicitCast("regrole"      , "oid"          )
  implicit val regtype_oid           = ImplicitCast("regtype"      , "oid"          )
  implicit val reltime_interval      = ImplicitCast("reltime"      , "interval"     )
  implicit val text_bpchar           = ImplicitCast("text"         , "bpchar"       )
  implicit val text_geometry         = ImplicitCast("text"         , "geometry"     )
  implicit val text_name             = ImplicitCast("text"         , "name"         )
  implicit val text_regclass         = ImplicitCast("text"         , "regclass"     )
  implicit val text_varchar          = ImplicitCast("text"         , "varchar"      )
  implicit val time_interval         = ImplicitCast("time"         , "interval"     )
  implicit val time_timetz           = ImplicitCast("time"         , "timetz"       )
  implicit val timestamp_timestamptz = ImplicitCast("timestamp"    , "timestamptz"  )
  implicit val varbit_bit            = ImplicitCast("varbit"       , "bit"          )
  implicit val varchar_bpchar        = ImplicitCast("varchar"      , "bpchar"       )
  implicit val varchar_name          = ImplicitCast("varchar"      , "name"         )
  implicit val varchar_regclass      = ImplicitCast("varchar"      , "regclass"     )
  implicit val varchar_text          = ImplicitCast("varchar"      , "text"         )


}