import ReleaseTransformations._
import microsites._

// Library versions all in one place, for convenience and sanity.
lazy val catsVersion          = "1.1.0"
lazy val catsEffectVersion    = "0.10.1"
lazy val doobieVersion        = "0.5.2"
lazy val kindProjectorVersion = "0.9.6"
lazy val shapelessVersion     = "2.3.3"
lazy val paigesVersion        = "0.2.0"
lazy val betterFilesVersion   = "3.4.0"

// Our set of warts
lazy val warts =
  Warts.allBut(
    Wart.Any,            // false positives
    Wart.ArrayEquals,    // false positives
    Wart.Nothing,        // false positives
    Wart.Product,        // false positives
    Wart.Serializable,   // false positives
    Wart.ImplicitParameter, // false positives
    Wart.ImplicitConversion,
    Wart.PublicInference, // unspeakable types
    Wart.FinalVal // unspeakable types
  )

// run dependencyUpdates whenever we [re]load. Spooky eh?
onLoad in Global := { s => "dependencyUpdates" :: s }

lazy val scalacSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
    "-encoding", "utf-8",                // Specify character encoding used by source files.
    "-explaintypes",                     // Explain type errors in more detail.
    "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
    "-language:higherKinds",             // Allow higher-kinded types
    "-language:implicitConversions",     // Allow definition of implicit functions called views
    "-language:postfixOps",
    "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
    "-Xfuture",                          // Turn on future language features.
    "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
    "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
    "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
    "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
    "-Xlint:option-implicit",            // Option.apply used implicit view.
    "-Xlint:package-object-classes",     // Class or object defined in package object.
    "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
    "-Xlint:unsound-match",              // Pattern match may not be typesafe.
    "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    // "-Yno-imports",                      // No predef or default imports
    "-Ypartial-unification",             // Enable partial unification in type constructor inference
    "-Ywarn-dead-code",                  // Warn when dead code is identified.
    "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
    "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
    "-Ywarn-numeric-widen",              // Warn when numerics are widened.
    "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals",              // Warn if a local definition is unused.
    "-Ywarn-unused:params",              // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates",            // Warn if a private member is unused.
    "-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.
    "-Yinduction-heuristics",            // speeds up the compilation of inductive implicit resolution
    // "-Ykind-polymorphism",               // type and method definitions with type parameters of arbitrary kinds
    "-Yliteral-types",                   // literals can appear in type position
    "-Xstrict-patmat-analysis",          // more accurate reporting of failures of match exhaustivity
    "-Xlint:strict-unsealed-patmat"      // warn on inexhaustive matches against unsealed traits
  ),
  scalacOptions in (Test, compile) --= Seq(
    "-Ywarn-unused:privates",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:imports",
    "-Yno-imports"
  ),
  scalacOptions in (Compile, console) --= Seq("-Xfatal-warnings", "-Ywarn-unused:imports", "-Yno-imports"),
  scalacOptions in (Compile, doc)     --= Seq("-Xfatal-warnings", "-Ywarn-unused:imports", "-Yno-imports")
)

lazy val commonSettings = scalacSettings ++ Seq(
  organization := "org.tpolecat",
  licenses ++= Seq(("MIT", url("http://opensource.org/licenses/MIT"))),
  scalaOrganization := "org.typelevel",
  scalaVersion := "2.12.4-bin-typelevel-4",

  // These sbt-header settings can't be set in ThisBuild for some reason
  headerMappings := headerMappings.value + (HeaderFileType.scala -> HeaderCommentStyle.cppStyleLineComment),
  headerLicense  := Some(HeaderLicense.Custom(
    """|Copyright (c) 2013-2018 Rob Norris
       |This software is licensed under the MIT License (MIT).
       |For more information see LICENSE or https://opensource.org/licenses/MIT
       |""".stripMargin
  )),

  // Wartremover in compile and test (not in Console)
  wartremoverErrors in (Compile, compile) := warts,
  wartremoverErrors in (Test,    compile) := warts,

  scalacOptions in (Compile, doc) ++= Seq(
    "-groups",
    "-sourcepath", (baseDirectory in LocalRootProject).value.getAbsolutePath,
    "-doc-source-url", "https://github.com/tpolecat/doobie-qb/blob/v" + version.value + "€{FILE_PATH}.scala"
  ),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % kindProjectorVersion),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  releaseProcess := Nil,
)

lazy val publishSettings = Seq(
  useGpg := false,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  homepage := Some(url("https://github.com/tpolecat/doobie-labs")),
  pomIncludeRepository := Function.const(false),
  pomExtra := (
    <developers>
      <developer>
        <id>tpolecat</id>
        <name>Rob Norris</name>
        <url>http://tpolecat.org</url>
      </developer>
    </developers>
  ),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value
)

lazy val noPublishSettings = Seq(
  skip in publish := true
)

lazy val labs = project.in(file("."))
  .settings(commonSettings)
  .settings(noPublishSettings)
  .dependsOn(qb, pg, docs)
  .aggregate(qb, pg, docs)
  .settings(
    name := "doobie-labs",
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      releaseStepCommand("docs/tut"), // annoying that we have to do this twice
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      releaseStepCommand("sonatypeReleaseAll"),
      releaseStepCommand("docs/publishMicrosite"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

lazy val qb = project
  .in(file("modules/qb"))
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    name := "doobie-labs-qb",
    description := "experimental query builder for doobie",
    libraryDependencies ++= Seq(
      "org.tpolecat"  %% "doobie-core"     % doobieVersion,
      "org.tpolecat"  %% "doobie-postgres" % doobieVersion,
      "com.github.pathikrit" %% "better-files" % betterFilesVersion,
      "org.typelevel" %% "cats-testkit"    % catsVersion % "test"
    ),
    initialCommands := """
      |import cats._, cats.implicits._
      |import cats.effect._, cats.effect.implicits._
      |import doobie._, doobie.implicits._
      |import doobie.postgres._, doobie.postgres.implicits._
      |import doobie.labs.qb._
      |import shapeless._
      |val xa = Transactor.fromDriverManager[IO](
      |  "org.postgresql.Driver",
      |  "jdbc:postgresql:world",
      |  "postgres", ""
      |)
      |val y = xa.yolo
      |import y._
    """.stripMargin
  )

lazy val pg = project
  .in(file("modules/pg"))
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    name := "doobie-labs-pg",
    description := "experimental query builder for doobie",
    libraryDependencies ++= Seq(
      "org.tpolecat"  %% "doobie-core"     % doobieVersion,
      "org.tpolecat"  %% "doobie-postgres" % doobieVersion,
      "com.github.pathikrit" %% "better-files" % betterFilesVersion,
      "org.typelevel" %% "cats-testkit"    % catsVersion % "test"
    ),
    initialCommands := """
      |import cats._, cats.implicits._
      |import cats.effect._, cats.effect.implicits._
      |import doobie._, doobie.implicits._
      |import doobie.postgres._, doobie.postgres.implicits._
      |import doobie.labs.qb._
      |import shapeless._
      |val xa = Transactor.fromDriverManager[IO](
      |  "org.postgresql.Driver",
      |  "jdbc:postgresql:world",
      |  "postgres", ""
      |)
      |val y = xa.yolo
      |import y._
    """.stripMargin
  )

lazy val docs = project
  .in(file("modules/docs"))
  .dependsOn(qb)
  .enablePlugins(MicrositesPlugin)
  .settings(commonSettings)
  .settings(noPublishSettings)
  .settings(
    scalacOptions in (Compile, console) += "-Xfatal-warnings", // turn this back on for tut
    libraryDependencies ++= Seq(
      // doc dependencies here
    ),
    fork in Test := true,
    // Settings for sbt-microsites https://47deg.github.io/sbt-microsites/
    micrositeImgDirectory     := baseDirectory.value / "src/main/resources/microsite/img",
    micrositeName             := "doobie-labs",
    micrositeDescription      := "Doobie Labs",
    micrositeAuthor           := "Rob Norris",
    micrositeGithubOwner      := "tpolecat",
    micrositeGithubRepo       := "doobie-labs",
    micrositeGitterChannel    := false, // no me gusta
    micrositeBaseUrl          := "/doobie-labs/",
    micrositeDocumentationUrl := "https://www.javadoc.io/doc/org.tpolecat/doobie-qb.12",
    micrositeHighlightTheme   := "color-brewer",
    // micrositePalette := Map(
    //   "brand-primary"     -> "#E35D31",
    //   "brand-secondary"   -> "#B24916",
    //   "brand-tertiary"    -> "#B24916",
    //   "gray-dark"         -> "#453E46",
    //   "gray"              -> "#837F84",
    //   "gray-light"        -> "#E3E2E3",
    //   "gray-lighter"      -> "#F4F3F4",
    //   "white-color"       -> "#FFFFFF"
    // ),
    micrositeConfigYaml := ConfigYml(
      yamlCustomProperties = Map(
        "doobie-qbVersion"    -> version.value,
        "catsVersion"     -> catsVersion,
        "scalaVersion"    -> scalaVersion.value,
        "scalaVersions"   -> crossScalaVersions.value.map(CrossVersion.partialVersion).flatten.map(_._2).mkString("2.", "/", "") // 2.11/12
      )
    ),
    micrositeExtraMdFiles := Map(
      file("CHANGELOG.md") -> ExtraMdFileConfig("changelog.md", "page", Map("title" -> "changelog", "section" -> "changelog", "position" -> "3")),
      file("LICENSE")      -> ExtraMdFileConfig("license.md",   "page", Map("title" -> "license",   "section" -> "license",   "position" -> "4"))
    )
  )
