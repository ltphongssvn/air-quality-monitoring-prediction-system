// air-quality-monitoring-prediction-system/spark-jobs/src/main/scala/ml/AQIPredictionPipeline.scala
package ml

import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{VectorAssembler, StandardScaler}
import org.apache.spark.ml.regression.{RandomForestRegressor, RandomForestRegressionModel}
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.sql.functions._

object AQIPredictionPipeline {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("AQI Prediction Pipeline")
      .getOrCreate()

    val dataPath = sys.env.getOrElse("TRAINING_DATA_PATH", "data/aqi_historical.parquet")
    val modelPath = sys.env.getOrElse("MODEL_OUTPUT_PATH", "models/aqi_predictor")

    val data = spark.read.parquet(dataPath)

    val featureCols = Array("pm25", "pm10", "o3", "no2", "co", "temperature", "humidity", "windSpeed")
    
    val assembler = new VectorAssembler()
      .setInputCols(featureCols)
      .setOutputCol("rawFeatures")
      .setHandleInvalid("skip")

    val scaler = new StandardScaler()
      .setInputCol("rawFeatures")
      .setOutputCol("features")
      .setWithStd(true)
      .setWithMean(true)

    val rf = new RandomForestRegressor()
      .setLabelCol("aqi")
      .setFeaturesCol("features")
      .setNumTrees(100)
      .setMaxDepth(10)
      .setSeed(42)

    val pipeline = new Pipeline()
      .setStages(Array(assembler, scaler, rf))

    val Array(trainData, testData) = data.randomSplit(Array(0.8, 0.2), seed = 42)

    val model = pipeline.fit(trainData)

    val predictions = model.transform(testData)

    val evaluator = new RegressionEvaluator()
      .setLabelCol("aqi")
      .setPredictionCol("prediction")

    val rmse = evaluator.setMetricName("rmse").evaluate(predictions)
    val r2 = evaluator.setMetricName("r2").evaluate(predictions)

    println(s"RMSE: $rmse")
    println(s"R2: $r2")

    model.write.overwrite().save(modelPath)

    spark.stop()
  }

  def loadModel(spark: SparkSession, modelPath: String): org.apache.spark.ml.PipelineModel = {
    org.apache.spark.ml.PipelineModel.load(modelPath)
  }
}
