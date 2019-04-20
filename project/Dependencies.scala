import sbt._

object Dependencies {
  val streamVersion = "2.5.20"
  val httpVersion = "10.1.7"
  val akka = {
    "com.typesafe.akka" %% "akka-stream" % streamVersion ::
      "com.typesafe.akka" %% "akka-http" % httpVersion ::
      "de.heikoseeberger" %% "akka-http-circe" % "1.25.2" ::
      Nil
  }

  val test = {
    val scalatestVersion = "3.0.5"
    val scalamockVersion = "4.1.0"
    "org.scalactic" %% "scalactic" % scalatestVersion ::
      "org.scalatest" %% "scalatest" % scalatestVersion ::
      "org.scalamock" %% "scalamock" % scalamockVersion ::
      "com.typesafe.akka" %% "akka-stream-testkit" % streamVersion ::
      "com.typesafe.akka" %% "akka-http-testkit" % httpVersion ::
      Nil
  }.map(_ % Test)

  val json = {
    val circeVersion = "0.10.1"

    ("io.circe" %% "circe-core" ::
      "io.circe" %% "circe-generic" ::
      "io.circe" %% "circe-parser" ::
      Nil) map (_ % circeVersion)
  }

  val logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2" ::
    "ch.qos.logback" % "logback-classic" % "1.2.3" :: Nil
}
