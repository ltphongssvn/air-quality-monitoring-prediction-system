# air-quality-monitoring-prediction-system/backend/Dockerfile
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN apk add --no-cache bash curl && \
    curl -fL https://github.com/sbt/sbt/releases/download/v1.9.9/sbt-1.9.9.tgz | tar xz && \
    ./sbt/bin/sbt stage

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache bash
COPY --from=builder /app/target/universal/stage /app
EXPOSE 9000
CMD ["bin/air-quality-backend"]
