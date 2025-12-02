// air-quality-monitoring-prediction-system/spark-jobs/src/main/scala/ml/AnomalyDetector.scala
package ml

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

object AnomalyDetector {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("AQI Anomaly Detector")
      .getOrCreate()

    import spark.implicits._

    val dataPath = sys.env.getOrElse("AQI_DATA_PATH", "data/aqi_readings.parquet")
    val data = spark.read.parquet(dataPath)

    // Detect anomalies using statistical methods
    val anomalies = detectStatisticalAnomalies(spark, data)
    anomalies.show()

    spark.stop()
  }

  def detectStatisticalAnomalies(spark: SparkSession, data: org.apache.spark.sql.DataFrame): org.apache.spark.sql.DataFrame = {
    import spark.implicits._

    // Calculate rolling statistics per sensor
    val windowSpec = Window.partitionBy("sensorId")
      .orderBy("timestamp")
      .rowsBetween(-24, 0) // 24-hour rolling window

    val withStats = data
      .withColumn("rolling_mean", avg("aqi").over(windowSpec))
      .withColumn("rolling_std", stddev("aqi").over(windowSpec))

    // Z-score based anomaly detection (threshold = 3 standard deviations)
    val withZScore = withStats
      .withColumn("z_score", 
        when(col("rolling_std") > 0, 
          (col("aqi") - col("rolling_mean")) / col("rolling_std")
        ).otherwise(0.0))
      .withColumn("is_anomaly", abs(col("z_score")) > 3.0)

    // IQR-based anomaly detection
    val withIQR = withZScore
      .withColumn("q1", expr("percentile_approx(aqi, 0.25)").over(Window.partitionBy("sensorId")))
      .withColumn("q3", expr("percentile_approx(aqi, 0.75)").over(Window.partitionBy("sensorId")))
      .withColumn("iqr", col("q3") - col("q1"))
      .withColumn("lower_bound", col("q1") - 1.5 * col("iqr"))
      .withColumn("upper_bound", col("q3") + 1.5 * col("iqr"))
      .withColumn("is_iqr_anomaly", col("aqi") < col("lower_bound") || col("aqi") > col("upper_bound"))

    // Spike detection (sudden change > 50 AQI points)
    val lagWindowSpec = Window.partitionBy("sensorId").orderBy("timestamp")
    val withSpike = withIQR
      .withColumn("prev_aqi", lag("aqi", 1).over(lagWindowSpec))
      .withColumn("aqi_change", abs(col("aqi") - col("prev_aqi")))
      .withColumn("is_spike", col("aqi_change") > 50)

    // Combined anomaly flag
    withSpike
      .withColumn("anomaly_type",
        when(col("is_anomaly") && col("is_spike"), "z_score_and_spike")
        .when(col("is_anomaly"), "z_score")
        .when(col("is_iqr_anomaly"), "iqr")
        .when(col("is_spike"), "spike")
        .otherwise("normal"))
      .filter(col("anomaly_type") =!= "normal")
      .select("sensorId", "timestamp", "aqi", "anomaly_type", "z_score", "aqi_change")
  }

  def detectSensorMalfunction(spark: SparkSession, data: org.apache.spark.sql.DataFrame): org.apache.spark.sql.DataFrame = {
    import spark.implicits._

    // Detect stuck sensors (same value for extended period)
    val windowSpec = Window.partitionBy("sensorId").orderBy("timestamp").rowsBetween(-10, 0)
    
    data
      .withColumn("unique_values", size(collect_set("aqi").over(windowSpec)))
      .withColumn("is_stuck", col("unique_values") === 1)
      .filter(col("is_stuck"))
      .select("sensorId", "timestamp", "aqi")
      .distinct()
  }
}
