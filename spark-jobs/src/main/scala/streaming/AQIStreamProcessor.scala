// air-quality-monitoring-prediction-system/spark-jobs/src/main/scala/streaming/AQIStreamProcessor.scala
package streaming

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._

object AQIStreamProcessor {

  val aqiSchema: StructType = StructType(Seq(
    StructField("sensorId", StringType, nullable = false),
    StructField("latitude", DoubleType, nullable = false),
    StructField("longitude", DoubleType, nullable = false),
    StructField("aqi", IntegerType, nullable = false),
    StructField("pm25", DoubleType, nullable = false),
    StructField("pm10", DoubleType, nullable = false),
    StructField("o3", DoubleType, nullable = false),
    StructField("no2", DoubleType, nullable = false),
    StructField("co", DoubleType, nullable = false),
    StructField("timestamp", TimestampType, nullable = false)
  ))

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("AQI Stream Processor")
      .config("spark.sql.streaming.checkpointLocation", "/tmp/checkpoints/aqi")
      .getOrCreate()

    import spark.implicits._

    val kafkaBootstrap = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
    val inputTopic = sys.env.getOrElse("KAFKA_INPUT_TOPIC", "aqi-raw")
    val outputTopic = sys.env.getOrElse("KAFKA_OUTPUT_TOPIC", "aqi-processed")

    val rawStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrap)
      .option("subscribe", inputTopic)
      .option("startingOffsets", "latest")
      .load()

    val parsedStream = rawStream
      .selectExpr("CAST(value AS STRING) as json")
      .select(from_json($"json", aqiSchema).as("data"))
      .select("data.*")

    // 5-minute window aggregation
    val aggregated = parsedStream
      .withWatermark("timestamp", "1 minute")
      .groupBy(
        window($"timestamp", "5 minutes"),
        $"sensorId"
      )
      .agg(
        avg("aqi").as("avg_aqi"),
        max("aqi").as("max_aqi"),
        avg("pm25").as("avg_pm25"),
        count("*").as("reading_count")
      )

    val output = aggregated
      .select(to_json(struct($"*")).as("value"))
      .writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrap)
      .option("topic", outputTopic)
      .trigger(Trigger.ProcessingTime("30 seconds"))
      .start()

    output.awaitTermination()
  }
}
