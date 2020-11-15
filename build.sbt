//kotlinSource in Compile := baseDirectory.value / "app/common"
resolvers += Resolver.bintrayRepo("kotlin", "kotlin-plugin")

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """YALTA-backend""",
    version := "2.8.x",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      guice,
      "com.typesafe.play" %% "play-slick" % "5.0.0",
      "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
      "com.h2database" % "h2" % "1.4.199",
      "com.byteslounge" %% "slick-repo" % "1.5.3",
//Can be added to eliminate duplicated code around kotlin models
//      "org.jetbrains.kotlinx" % "kotlinx-serialization-core" % "1.0.1",
//      "org.jetbrains.kotlinx" % "kotlinx-serialization-json-jvm" % "1.0.1",
//      "org.jetbrains.kotlin.plugin.serialization" % "org.jetbrains.kotlin.plugin.serialization.gradle.plugin" %
//        "1.4.10-release-411" % "compile-internal",
      "com.google.code.gson" % "gson" % "2.8.6",
      "org.camunda.connect" % "camunda-connect-core" % "1.5.0",
      "org.camunda.connect" % "camunda-connect-connectors-all" % "1.5.0",

      specs2 % Test,
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
kotlinVersion := "1.4.10"
