name := "backend"

version := "SNAPSHOT"

organization := "name.dmitrym"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.9",
  "com.softwaremill.akka-http-session" %% "core" % "0.2.6",
  "com.softwaremill.akka-http-session" %% "jwt" % "0.2.6",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1",
  "org.jsoup" % "jsoup" % "1.9.2",
  "org.apache.spark" %% "spark-mllib" % "2.0.0",
  "databricks" %% "spark-corenlp" % "0.3-SNAPSHOT",
  "edu.stanford.nlp" % "stanford-corenlp" %  "3.6.0" classifier "models-english",
  "org.mongodb.spark" %% "mongo-spark-connector" % "2.0.0",
  "org.specs2" %% "specs2-core" % "3.8.4" % "test",
  "org.specs2" %% "specs2-scalacheck" % "3.8.4" % "test"
)

scalacOptions ++= Seq(
  "-deprecation", "-feature", "-unchecked", "-Xlint:_", "-Ydead-code", "-Yopt:_", "-Ywarn-dead-code",
  "-Ywarn-numeric-widen", "-Ywarn-unused", "-Ywarn-unused-import", "-Ywarn-value-discard"
  )

scalacOptions in Test ++= Seq(
  "-Yrangepos"
  )

javacOptions ++= Seq(
  "-deprecation", "-Xlint"
  )

incOptions := incOptions.value.withNameHashing(true)
