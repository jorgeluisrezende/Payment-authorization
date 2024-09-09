val scala3Version = "3.5.0"
val http4sVersion = "0.23.27"
val http4sBlaze = "0.23.13"
val circeVersion = "0.14.8"

scalacOptions ++= Seq(
  "-Xmax-inlines:64"
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "Scala 3 Project Template",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sBlaze,
      "org.http4s" %% "http4s-blaze-client" % http4sBlaze,
      "org.http4s" %% "http4s-circe"        % http4sVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.tpolecat" %% "skunk-core" % "0.6.3",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.5"
    )
  )
