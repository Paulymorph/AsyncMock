lazy val `async-mock` = (project in file("."))
  .settings(
    name := "AsyncMock",
    version := "0.1.0",
    scalaVersion := "2.12.8",
    libraryDependencies ++= {
      import Dependencies._
      akka ++ test ++ json ++ logging
    },
    mainClass in assembly := Some("Main"),
    assemblyJarName in assembly := s"${name.value}.jar",
    scalacOptions ++= "-Xfatal-warnings" :: "-unchecked" :: "-deprecation" :: Nil
  )
