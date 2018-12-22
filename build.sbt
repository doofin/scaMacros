val thisScalaVersion = "2.12.7"
resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases",
  "scala-integration" at "https://scala-ci.typesafe.com/artifactory/scala-integration/"
)
cancelable in Scope.Global := true

scalaVersion in ThisBuild := thisScalaVersion

val macroDeps = Seq(
  "org.scala-lang" % "scala-reflect" % thisScalaVersion,
  "org.scalameta"  %% "scalameta"    % "1.8.0"
)

val macroApplyDeps = Seq(
  "edu.berkeley.cs" %% "chisel3"            % "3.1.2",
  "edu.berkeley.cs" %% "firrtl-interpreter" % "1.1.2"
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")

val scalacOptionsAll = Seq(
  "-feature",
  "-Xfuture",
  "-language:higherKinds,implicitConversions",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-deprecation",
  "-unchecked",
  "-Ypartial-unification",
  "-language:postfixOps"
)

val macroSettings = Seq(
  libraryDependencies ++= macroDeps,
  addCompilerPlugin("org.scalameta" %% "paradise" % "3.0.0-M11" cross CrossVersion.full)
)

lazy val macros = (project in file("macros"))
  .settings(macroSettings: _*)

lazy val usage = project
  .in(file("usage"))
  .dependsOn(macros) // macro!
  .settings(macroSettings: _*)
  .settings(libraryDependencies ++= macroApplyDeps, scalacOptions += "-Xsource:2.11")

lazy val root = (project in file(".")).settings( // can not use macro here!
  organization := "com.cuttingedge",
  version := "0.2",
  name := "cutting-edge",
  libraryDependencies ++= Seq(),
  scalacOptions ++= scalacOptionsAll
)

