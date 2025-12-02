// air-quality-monitoring-prediction-system/spark-jobs/build.sbt
name := "air-quality-spark-jobs"
version := "0.1.0"
scalaVersion := "2.12.18"

val sparkVersion = "3.5.0"

// Set default main class for streaming
Compile / mainClass := Some("streaming.AQIStreamProcessor")
Compile / packageBin / mainClass := Some("streaming.AQIStreamProcessor")

libraryDependencies ++= Seq(
  // Spark Core
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-graphx" % sparkVersion % "provided",
  // Kafka integration
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  // Breeze for numerical computing
  "org.scalanlp" %% "breeze" % "2.1.0",
  "org.scalanlp" %% "breeze-viz" % "2.1.0",
  // JSON
  "com.typesafe.play" %% "play-json" % "2.9.4",
  // Testing
  "org.scalatest" %% "scalatest" % "3.2.17" % Test
)

// Assembly settings
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
