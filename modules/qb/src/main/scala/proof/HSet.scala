package doobie.labs.qb
package proof

import shapeless._

// type-level set
object HSet {

  final class Contains[S <: HList, A]
  object Contains {

    implicit def head[H, T <: HList]: Contains[H :: T, H] =
      new Contains[H :: T, H]

    implicit def tail[H, T <: HList, A](
      implicit ev: Contains[T, A]
    ): Contains[H :: T, A] = {
      void(ev)
      new Contains[H :: T, A]
    }

  }

  sealed trait Remove[S <: HList, A] {
    type Out <: HList
  }
  object Remove extends RemoveLow {
    type Aux[S <: HList, A, O <: HList] = Remove[S, A] { type Out = O }

    implicit def hnil[A]: Remove.Aux[HNil, A, HNil] =
      new Remove[HNil, A] {
        type Out = HNil
      }

    implicit def hconsIn[H, T <: HList]: Remove.Aux[H :: T, H, T] =
      new Remove[H :: T, H] {
        type Out = T
      }
  }
  trait RemoveLow {

    implicit def hconsOut[H, T <: HList, A](
      implicit ev: Remove[T, A]
    ): Remove.Aux[H :: T, A, H :: ev.Out] =
      new Remove[H :: T, A] {
        type Out = H :: ev.Out
      }

  }

  sealed trait Add[S <: HList, A] {
    type Out <: HList
  }
  object Add {
    type Aux[S <: HList, A, O <: HList] = Add[S, A] { type Out = O }

    implicit def hnil[H]: Add.Aux[HNil, H, H :: HNil] =
      new Add[HNil, H] {
        type Out = H :: HNil
      }

    implicit def hcons[H, T <: HList](
      implicit ev: Remove[T, H]
    ): Add.Aux[T, H, H :: ev.Out] =
      new Add[T, H] {
        type Out = H :: ev.Out
      }

  }

  sealed trait Intersect[A <: HList, B <: HList] {
    type Out <: HList
  }
  object Intersect extends IntersectLow {
    type Aux[A <: HList, B <: HList, O <: HList] = Intersect[A, B] { type Out = O }

    implicit def hnil1[A <: HList]: Intersect.Aux[A, HNil, HNil] =
      new Intersect[A, HNil] {
        type Out = HNil
      }

    implicit def hconsIn[H, T <: HList, B <: HList](
      implicit ev: Contains[B, H],
               in: Intersect[T, B]
    ): Intersect.Aux[H :: T, B, H :: in.Out] =
      new Intersect[H :: T, B] {
        void(ev)
        type Out = H :: in.Out
      }

  }
  trait IntersectLow {

    implicit def hnil2[A <: HList]: Intersect.Aux[HNil, A, HNil] =
      new Intersect[HNil, A] {
        type Out = HNil
      }

    implicit def hconsOut[H, T <: HList, B <: HList](
      implicit in: Intersect[T, B]
    ): Intersect.Aux[H :: T, B, in.Out] =
      new Intersect[H :: T, B] {
        type Out = in.Out
      }

  }

  val a = (
    implicitly[Contains[String :: HNil, String]],
    implicitly[Contains[String :: Int :: Boolean :: HNil, Int]],
    implicitly[Remove.Aux[String :: Boolean :: HNil, Boolean, String :: HNil]],
    implicitly[Add.Aux[String :: Boolean :: HNil, Int, Int :: String :: Boolean :: HNil]],
    implicitly[Add.Aux[String :: Boolean :: HNil, Boolean, Boolean :: String :: HNil]],
    implicitly[Intersect.Aux[HNil, HNil, HNil]],
    implicitly[Intersect.Aux[Int :: HNil, HNil, HNil]],
    implicitly[Intersect.Aux[HNil, Int :: HNil, HNil]],
    implicitly[Intersect.Aux[Int :: HNil, Int :: HNil, Int :: HNil]],
    implicitly[Intersect.Aux[Int :: String :: HNil, Int :: HNil, Int :: HNil]],
    implicitly[Intersect.Aux[Int :: HNil, Int :: String :: HNil, Int :: HNil]],
    implicitly[Intersect.Aux[Int :: Boolean :: String :: HNil, String :: Boolean :: Long :: HNil, Boolean :: String :: HNil]],
  )

}