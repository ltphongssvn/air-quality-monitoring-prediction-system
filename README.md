<!-- air-quality-monitoring-prediction-system/README.md -->
# Air Quality Monitoring & Prediction System

## Project Overview
Real-time air quality monitoring and prediction system using Scala, Spark, and Play Framework.

## Tech Stack
- **Backend:** Play Framework (Scala), Akka Actors
- **Data Processing:** Spark Streaming, MLlib, GraphX, Breeze
- **Frontend:** React, D3.js
- **Databases:** MongoDB, PostgreSQL (Slick)
- **Deployment:** GCP (Dataproc, Cloud SQL, GKE)

## Data Sources
- **Streaming:** OpenWeatherMap API, PurpleAir API, EPA AirNow API
- **Batch:** EPA Historical Air Quality Data, NOAA Climate Data

---

## Sprint Planning

### Sprint 0: Project Initialization (Current)
- [x] Git repository initialized
- [x] GitFlow branches created (main, develop)
- [x] .gitignore configured
- [x] README documentation
- [x] Project directory structure
- [x] SBT build configuration - **VERIFIED**

---

## Development Log

### 2024-12-01: Backend SBT Setup
**Status:** ✅ SUCCESS
```bash
cd backend && sbt compile
```

**Output (summarized):**
```
[info] welcome to sbt 1.9.9 (Eclipse Adoptium Java 17.0.13)
[info] loading settings for project root from build.sbt
[info] Play is run entirely by the community.
[success] Total time: 8 s
```

**Files created:**
- `backend/build.sbt` - Play Framework dependencies
- `backend/project/plugins.sbt` - SBT plugins (Play 2.9.0)
- `backend/project/build.properties` - SBT version 1.9.9

---

## Git Workflow
- `main`: Production releases
- `develop`: Integration branch
- `feature/*`: Feature branches from develop

## Project Structure
```
├── backend/          # Play Framework (Scala)
├── frontend/         # React + D3.js
├── spark-jobs/       # Spark Streaming, ML, GraphX
└── docs/             # Documentation
```

## Docker Deployment

### docker-compose.yml
**Status:** ✅ VERIFIED
```bash
docker-compose config
# No warnings with .env configured
```

**Services:**
- backend (Play Framework) - port 9000
- frontend (React/Nginx) - port 3000
- postgres (PostgreSQL 15) - port 5432
- mongodb (MongoDB 7) - port 27017

**Setup:**
```bash
cp .env.example .env
# Edit .env with your API keys
docker-compose up -d
```

### Docker Compose V2 Fix
**Status:** ✅ VERIFIED
```bash
docker compose config --quiet
# No errors or warnings
```

**Change:** Removed deprecated `version: '3.8'` attribute

## Sprint 6: Local Component Testing

### Epic: Verify All System Components

**Tasks:**
- [x] Play Framework REST APIs (GET /api/v1/aqi)
- [x] MongoDB connection and operations
- [x] PostgreSQL/Slick connection
- [x] External API Services (OpenWeatherMap ✅, PurpleAir ✅, EPA AirNow ✅)
- [x] React Frontend Dashboard rendering
- [x] Akka Actors (SensorMonitorActor compiled, AkkaHttpServer running)
- [x] Spark Jobs compilation (Streaming, ML, GraphX, Breeze)

## Phase 1: Infrastructure Verification (per CSCI-E88C Final Project)

### Epic: Verify Infrastructure Components

**Tasks:**
- [x] Spark cluster setup and connectivity (Master UI at :8090)
- [x] Kafka setup for stream buffering (topic aqi-raw created)
- [x] MongoDB database (sensor metadata, real-time readings)
- [x] PostgreSQL database (historical aggregations)
- [x] Play Framework API configuration
- [x] API integrations (OpenWeatherMap, PurpleAir, EPA AirNow)
- [x] Basic data ingestion pipeline (Kafka produce/consume verified)

### Spark Cluster Verified
**Status:** ✅ SUCCESS
```bash
docker compose up -d zookeeper kafka spark-master spark-worker
# All 4 containers started successfully

docker compose ps
# spark-master: Up, ports 7077, 8090
# spark-worker: Up, connected to master
# kafka: Up, ports 9092, 29092
# zookeeper: Up, port 2181
```

## Phase 2: Core Processing (per CSCI-E88C Final Project)

### Epic: Develop Processing Pipeline

**Tasks:**
- [ ] Spark Streaming transformations (real-time AQI processing)
- [ ] AQI calculation algorithms
- [ ] Actor-based monitoring system (Akka)
- [x] Kafka topic aqi-processed creation
- [ ] End-to-end streaming test

### Kafka Topic aqi-processed Created
**Status:** ✅ SUCCESS
```bash
docker exec air-quality-monitoring-prediction-system-kafka-1 kafka-topics --create --topic aqi-processed --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
# Created topic aqi-processed.
```

### Docker Network Issue
**Status:** ❌ ERROR
```bash
docker compose up -d
# Error response from daemon: failed to set up container networking: network e2f2779b568c500dafdb5b69b90e41cb67ad968bfa36a70dd8d5be7fa6eaed9e not found
```

**Fix:** Need to recreate Docker network

### Docker Network Fix
**Status:** ✅ RESOLVED
```bash
docker compose down && docker compose up -d
# Network air-quality-monitoring-prediction-system_default Created
# All 9 containers started successfully
```

### Kafka Topics Recreated After Reset
**Status:** ✅ SUCCESS
```bash
docker exec air-quality-monitoring-prediction-system-kafka-1 kafka-topics --create --topic aqi-raw --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
# Created topic aqi-raw.
```
```bash
docker exec air-quality-monitoring-prediction-system-kafka-1 kafka-topics --create --topic aqi-processed --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
# Created topic aqi-processed.
```

### Spark Jobs Package
**Status:** ✅ SUCCESS (warning to fix)
```bash
cd spark-jobs && sbt package
# [warn] multiple main classes detected

sbt "show discoveredMainClasses"
# * graphx.PollutionSpreadGraph
# * ml.AQIPredictionPipeline
# * streaming.AQIStreamProcessor
```

**Fix needed:** Configure primary main class in build.sbt

### Spark mainClass Fix Applied
**Status:** ✅ FIXED
```bash
# Added to spark-jobs/build.sbt:
Compile / mainClass := Some("streaming.AQIStreamProcessor")
Compile / packageBin / mainClass := Some("streaming.AQIStreamProcessor")

cd spark-jobs && sbt package
# [success] Total time: 4 s
```

### Spark JAR Packaged
**Status:** ✅ SUCCESS
```bash
ls -la spark-jobs/target/scala-2.12/*.jar
# -rw-r--r-- 1 lenovo lenovo 27494 Dec  2 06:20 spark-jobs/target/scala-2.12/air-quality-spark-jobs_2.12-0.1.0.jar
```

### Spark JAR Copied to Master
**Status:** ✅ SUCCESS
```bash
docker exec air-quality-monitoring-prediction-system-spark-master-1 mkdir -p /opt/spark/work
docker cp spark-jobs/target/scala-2.12/air-quality-spark-jobs_2.12-0.1.0.jar air-quality-monitoring-prediction-system-spark-master-1:/opt/spark/work/
# Successfully copied 29.2kB to air-quality-monitoring-prediction-system-spark-master-1:/opt/spark/work/
```

### Spark Submit Attempt
**Status:** ❌ ERROR
```bash
docker exec air-quality-monitoring-prediction-system-spark-master-1 /opt/spark/bin/spark-submit --class streaming.AQIStreamProcessor --master spark://spark-master:7077 --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.0 /opt/spark/work/air-quality-spark-jobs_2.12-0.1.0.jar
# Exception: java.io.FileNotFoundException: /nonexistent/.ivy2/cache
# Issue: No home directory for ivy cache in container
```

**Fix needed:** Build assembly JAR with all dependencies included

### Spark Assembly JAR Built
**Status:** ✅ SUCCESS
```bash
cd spark-jobs && sbt assembly
# Built: spark-jobs/target/scala-2.12/air-quality-spark-jobs-assembly-0.1.0.jar
# Jar hash: 379d3b78f67bb15aa91a9c731137a04d2e58d651
# [success] Total time: 11 s
```

### Assembly JAR Copied to Spark Master
**Status:** ✅ SUCCESS
```bash
docker cp spark-jobs/target/scala-2.12/air-quality-spark-jobs-assembly-0.1.0.jar air-quality-monitoring-prediction-system-spark-master-1:/opt/spark/work/
# Successfully copied 107MB to air-quality-monitoring-prediction-system-spark-master-1:/opt/spark/work/
```

### Spark Streaming Job Submission
**Status:** ❌ ERROR - Job killed
```bash
docker exec -d air-quality-monitoring-prediction-system-spark-master-1 /opt/spark/bin/spark-submit --class streaming.AQIStreamProcessor --master spark://spark-master:7077 --conf spark.kafka.bootstrap.servers=kafka:9092 /opt/spark/work/air-quality-spark-jobs-assembly-0.1.0.jar

docker logs air-quality-monitoring-prediction-system-spark-master-1 2>&1 | tail -30
# 15:09:59 INFO Master: Registering app AQI Stream Processor
# 15:10:01 INFO Master: Removing app app-20251202150959-0000

docker logs air-quality-monitoring-prediction-system-spark-worker-1 2>&1 | tail -50
# Executor app-20251202150959-0000/0 finished with state KILLED exitStatus 143
```

**Issue:** Executor killed - need to check executor logs for root cause
