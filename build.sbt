organization := "com.phasmidsoftware"

name := "SecureCsv"

version := "0.0.2"

scalaVersion := "2.13.7"

scalacOptions += "-deprecation"

resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

lazy val scalaTestVersion = "3.2.9"

libraryDependencies ++= Seq(
  "com.phasmidsoftware" %%  "tableparser" % "1.1.2-SNAPSHOT",
  "com.phasmidsoftware" %%  "args" % "1.0.4-SNAPSHOT",
  "ch.qos.logback" % "logback-classic" % "1.2.10",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

assembly / assemblyJarName := "SecureCsv.jar"