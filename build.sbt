resolvers in Global += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// Library versions all in one place, for convenience and sanity.
lazy val betterFilesVersion   = "3.7.0"
lazy val catsEffectVersion    = "0.10.1"
lazy val catsVersion          = "1.6.0"
lazy val doobieVersion        = "0.7.0-M2"
lazy val kindProjectorVersion = "0.9.9"
lazy val shapelessVersion     = "2.3.3"

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
    "-Xlint",
    "-Yrangepos"
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
  scalaVersion := "2.13.0-M5",

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
)

lazy val labs = project.in(file("."))
  .settings(commonSettings)
  .dependsOn(qb, pg)
  .aggregate(qb, pg)
  .settings(
    name := "doobie-labs",
  )

lazy val qb = project
  .in(file("modules/qb"))
  .settings(commonSettings)
  .settings(
    name := "doobie-labs-qb",
    description := "experimental query builder for doobie",
    libraryDependencies ++= Seq(
      "org.tpolecat"  %% "doobie-core"     % doobieVersion,
      "org.tpolecat"  %% "doobie-postgres" % doobieVersion,
      "org.typelevel" %% "cats-testkit"    % catsVersion % "test",
      "com.github.pathikrit" %% "better-files" % betterFilesVersion,
    ),
  )

lazy val pg = project
  .in(file("modules/pg"))
  .settings(commonSettings)
  .settings(
    name := "doobie-labs-pg",
    description := "experimental query builder for doobie",
    libraryDependencies ++= Seq(
      "org.tpolecat"  %% "doobie-core"     % doobieVersion,
      "org.tpolecat"  %% "doobie-postgres" % doobieVersion,
      "org.typelevel" %% "cats-testkit"    % catsVersion % "test",
      "com.github.pathikrit" %% "better-files" % betterFilesVersion,
    ),
  )
