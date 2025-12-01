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
