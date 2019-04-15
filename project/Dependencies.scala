import sbt._

object Dependencies {
  val streamVersion = "2.5.20"
  val akka = {
    val httpVersion = "10.1.7"
    "com.typesafe.akka" %% "akka-stream" % streamVersion ::
      "com.typesafe.akka" %% "akka-http" % httpVersion ::
      "de.heikoseeberger" %% "akka-http-circe" % "1.25.2" ::
      Nil
  }

  val test = {
    val scalatestVersion = "3.0.5"
    val scalamockVersion = "4.1.0"
    "org.scalactic" %% "scalactic" % scalatestVersion ::
      "org.scalatest" %% "scalatest" % scalatestVersion % Test ::
      "org.scalamock" %% "scalamock" % scalamockVersion % Test ::
      "com.typesafe.akka" %% "akka-stream-testkit" % streamVersion % Test ::
      Nil
  }

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
