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
