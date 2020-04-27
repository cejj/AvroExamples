name := "AvroExamples"
version := "0.1"
scalaVersion := "2.12.11"

import sbt._
import Keys._

val scioVersion = "0.8.0-alpha2"
val beamVersion = "2.13.0"
val scalaMacrosVersion = "2.1.1"
val avroVersion = "1.8.2"
val protobufVersion = "3.11.4"

lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "common",
  // Semantic versioning http://semver.org/
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.8",
  scalacOptions ++= Seq("-target:jvm-1.8",
    "-deprecation",
    "-feature",
    "-unchecked"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

lazy val paradiseDependency =
  "org.scalamacros" % "paradise" % scalaMacrosVersion cross CrossVersion.full
lazy val macroSettings = Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  addCompilerPlugin(paradiseDependency)
)


//packMain := Map("EventProcessor" -> "org.ather.VehicleEvents.EventProcessor")
packMain := Map("ExampleInOut" -> "examples.ExampleInOut")

lazy val Root: Project = project
  .in(file("."))
  .settings(commonSettings)
  .settings(macroSettings)
  .settings(
    name := "EventProcessor",
    description := "WordCount",
    publish / skip := true,
    libraryDependencies ++= Seq(
      "com.spotify" %% "scio-core" % scioVersion,
      "com.spotify" %% "scio-test" % scioVersion % Test,
      "org.apache.avro" % "avro" % avroVersion,
      "com.spotify" %% "scio-bigquery" % scioVersion,
      "com.spotify" %% "scio-extra" % scioVersion,
      "com.spotify" %% "scio-avro" % scioVersion,
      "com.spotify" %% "scio-parquet" % scioVersion,
      "com.spotify" %% "scio-jdbc" % scioVersion,
      "com.google.cloud" % "google-cloud-bigquery" % "0.17.2-beta",
      "org.apache.beam" % "beam-runners-direct-java" % beamVersion,
      "org.apache.beam" % "beam-examples-java" % beamVersion,
      "org.postgresql" % "postgresql" % "9.4-1200-jdbc41",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "com.google.cloud.sql" % "postgres-socket-factory" % "1.0.3",
      // optional dataflow runner
      "org.apache.beam" % "beam-runners-google-cloud-dataflow-java" % beamVersion,
      "org.slf4j" % "slf4j-simple" % "1.7.25",
      "org.apache.beam" % "beam-sdks-java-extensions-sql" % beamVersion,
      "org.json4s" %% "json4s-core" % "3.6.7",
      "org.json4s" %% "json4s-native" % "3.6.7",
      "org.apache.beam" % "beam-sdks-java-io-parquet" % beamVersion
    )
  )
  .enablePlugins(PackPlugin)