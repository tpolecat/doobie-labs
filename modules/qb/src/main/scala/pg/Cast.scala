package doobie.labs.qb.pg

// select st.typname, tt.typname, castcontext, castmethod
// from pg_cast c
// join pg_type st on c.castsource = st.oid
// join pg_type tt on c.casttarget = tt.oid
// where castsource != casttarget;

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

object ExplicitCast {

  def apply[S <: String with Singleton, T <: String with Singleton](s: S, t: T): ExplicitCast[S, T] =
    new ExplicitCast[S, T] {
      val source = s
      val target = t
    }

}

trait AssignmentCast[S <: String with Singleton, T <: String with Singleton]
  extends ExplicitCast[S, T]

object AssignmentCast {

  def apply[S <: String with Singleton, T <: String with Singleton](s: S, t: T): AssignmentCast[S, T] =
    new AssignmentCast[S, T] {
      val source = s
      val target = t
    }


}

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

object ImplicitCast {

  def apply[S <: String with Singleton, T <: String with Singleton](s: S, t: T): ImplicitCast[S, T] =
    new ImplicitCast[S, T] {
      val source = s
      val target = t
    }


}