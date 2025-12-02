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
- [ ] Spark cluster setup and connectivity
- [ ] Kafka setup for stream buffering
- [ ] MongoDB database (sensor metadata, real-time readings)
- [ ] PostgreSQL database (historical aggregations)
- [ ] Play Framework API configuration
- [ ] API integrations (OpenWeatherMap, PurpleAir, EPA AirNow)
- [ ] Basic data ingestion pipeline

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
