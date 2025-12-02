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

    val anomalies = detectStatisticalAnomalies(spark, data)
    anomalies.show()

    spark.stop()
  }

  def detectStatisticalAnomalies(spark: SparkSession, data: org.apache.spark.sql.DataFrame): org.apache.spark.sql.DataFrame = {
    import spark.implicits._

    val windowSpec = Window.partitionBy("sensorId")
      .orderBy("timestamp")
      .rowsBetween(-24, 0)

    val withStats = data
      .withColumn("rolling_mean", avg("aqi").over(windowSpec))
      .withColumn("rolling_std", stddev("aqi").over(windowSpec))

    val withZScore = withStats
      .withColumn("z_score", 
        when(col("rolling_std") > 0, 
          (col("aqi") - col("rolling_mean")) / col("rolling_std")
        ).otherwise(0.0))
      .withColumn("is_anomaly", abs(col("z_score")) > 3.0)

    val withIQR = withZScore
      .withColumn("q1", expr("percentile_approx(aqi, 0.25)").over(Window.partitionBy("sensorId")))
      .withColumn("q3", expr("percentile_approx(aqi, 0.75)").over(Window.partitionBy("sensorId")))
      .withColumn("iqr", col("q3") - col("q1"))
      .withColumn("lower_bound", col("q1") - lit(1.5) * col("iqr"))
      .withColumn("upper_bound", col("q3") + lit(1.5) * col("iqr"))
      .withColumn("is_iqr_anomaly", col("aqi") < col("lower_bound") || col("aqi") > col("upper_bound"))

    val lagWindowSpec = Window.partitionBy("sensorId").orderBy("timestamp")
    val withSpike = withIQR
      .withColumn("prev_aqi", lag("aqi", 1).over(lagWindowSpec))
      .withColumn("aqi_change", abs(col("aqi") - col("prev_aqi")))
      .withColumn("is_spike", col("aqi_change") > 50)

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
}
