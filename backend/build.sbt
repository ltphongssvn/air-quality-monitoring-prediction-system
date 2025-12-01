// air-quality-monitoring-prediction-system/backend/build.sbt
name := "air-quality-backend"
version := "0.1.0"
scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

// Play Framework
libraryDependencies ++= Seq(
  guice,
  ws,
  
  // Akka
  "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
  "com.typesafe.akka" %% "akka-stream" % "2.8.5",
  
  // Database - Slick for PostgreSQL
  "com.typesafe.play" %% "play-slick" % "5.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.1.0",
  "org.postgresql" % "postgresql" % "42.6.0",
  
  // MongoDB
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.11.1",
  
  // JSON
  "com.typesafe.play" %% "play-json" % "2.10.3",
  
  // Testing
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
)

// Resolver for Akka
resolvers += "Akka library repository".at("https://repo.akka.io/maven")
