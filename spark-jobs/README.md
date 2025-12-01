<!-- air-quality-monitoring-prediction-system/spark-jobs/README.md -->
# Spark Jobs

Spark Streaming, MLlib, and GraphX jobs for air quality processing.

## Prerequisites
- Java 17+
- SBT 1.9.9
- Spark 3.5.0

## Build
```bash
sbt compile
# [success] Total time: 20 s
```

## Dependencies
- Spark Core, SQL, Streaming, MLlib, GraphX
- Kafka integration
- Breeze (numerical computing)

### AQIStreamProcessor Added
**Status:** ✅ SUCCESS
```bash
sbt compile
# [success] Total time: 5 s
```

**Files:**
- `src/main/scala/streaming/AQIStreamProcessor.scala`
  - Kafka streaming input/output
  - 5-minute window aggregations
  - Env vars: KAFKA_BOOTSTRAP_SERVERS, KAFKA_INPUT_TOPIC, KAFKA_OUTPUT_TOPIC

### AQIPredictionPipeline Added
**Status:** ✅ SUCCESS
```bash
sbt compile
# [success] Total time: 7 s
```

**Files:**
- `src/main/scala/ml/AQIPredictionPipeline.scala`
  - RandomForest regression model
  - Feature scaling, 80/20 train/test split
  - RMSE/R2 evaluation metrics
  - Env vars: TRAINING_DATA_PATH, MODEL_OUTPUT_PATH

### PollutionSpreadGraph Added
**Status:** ✅ SUCCESS
```bash
sbt compile
# [success] Total time: 6 s
```

**Files:**
- `src/main/scala/graphx/PollutionSpreadGraph.scala`
  - Sensor network graph with geographic edges
  - PageRank for pollution influence
  - Connected components for clusters
  - Pregel-based pollution propagation

### BreezeAnalytics Added
**Status:** ✅ SUCCESS
```bash
sbt compile
# [success] Total time: 13 s
```

**Files:**
- `src/main/scala/ml/BreezeAnalytics.scala`
  - Correlation matrix computation
  - IDW interpolation for AQI
  - Heatmap data generation
