# Enterprise Data Ingestion & Migration Engine

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-brightgreen.svg)
![Spring Batch](https://img.shields.io/badge/Spring%20Batch-6.0-green.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)
![Grafana](https://img.shields.io/badge/Observability-Prometheus%20%7C%20Grafana-F46800.svg)

A high-throughput, fault-tolerant batch processing engine built to securely migrate legacy data payloads (1,000,000+ records) into a modern relational database. Designed with enterprise constraints in mind, this project demonstrates multi-threaded chunk processing, thread-pool backpressure, zero-downtime error quarantine, and real-time telemetry.

---

## 📊 System Architecture & Observability

### Data Flow
1. **Trigger**: Asynchronous REST API initiates the migration.
2. **Read**: `SynchronizedItemStreamReader` streams massive CSVs from disk without loading the entire file into memory.
3. **Process**: Multi-threaded processors validate formats, sanitize strings, and parse dates.
4. **Write**: Batched JDBC inserts (`saveAll`) write chunks to PostgreSQL.
5. **Monitor**: Spring Actuator and Micrometer broadcast metrics to a Prometheus time-series DB, visualized in Grafana.

---

## 🛠️ Key Engineering Challenges Solved

This project was built to address common bottlenecks found in large-scale data migrations:

### 1. High-Performance Concurrency with Backpressure
* **The Problem:** Fast I/O disk readers easily overwhelm worker thread pools writing to a slower relational database, resulting in `TaskRejectedException` and crashed jobs.
* **The Solution:** Implemented a multi-threaded chunk step utilizing a `ThreadPoolTaskExecutor` (16 max threads) configured with a **`CallerRunsPolicy`**. When the internal queue reaches capacity, backpressure is applied to the reader thread, naturally throttling ingestion to match database write limits and preventing Out-Of-Memory (OOM) errors.
* 🔗 [View `BatchConfig.java`](src/main/java/com/project/migration/config/BatchConfig.java)

### 2. Fault-Tolerant Error Quarantine
* **The Problem:** A single malformed record (e.g., a corrupted date string) rolling back a transaction and failing a 1,000,000-record batch job.
* **The Solution:** Configured a `.faultTolerant()` step with a custom `CustomerSkipListener`. Records throwing `DataValidationException` are intercepted, isolated from the main transaction, and written to a dedicated `migration_item_error_log` audit table. The job completes successfully, leaving bad data quarantined for operational review.
* 🔗 [View `CustomerSkipListener.java`](src/main/java/com/project/migration/batch/listener/CustomerSkipListener.java)

### 3. Deterministic Schema Evolution
* **The Problem:** Relying on Hibernate's `ddl-auto: update` causes unpredictable schema changes in production.
* **The Solution:** Disabled Hibernate DDL execution (`ddl-auto: validate`) and integrated **Flyway**. Database state is strictly managed through versioned SQL scripts, ensuring JPA entities are always perfectly mapped to the underlying PostgreSQL schema.

### 4. Asynchronous REST Control Plane
* **The Problem:** HTTP requests block and timeout when triggering batch processes that take several minutes to run.
* **The Solution:** Exposed a decoupled REST API using Spring Batch's `JobOperator`. The `/trigger` endpoint injects unique run IDs and returns an immediate HTTP 202 (Accepted), allowing clients to poll execution status via a separate `/status/{jobId}` endpoint.

---

## 💻 Technology Stack

* **Core Backend:** Java 21, Spring Boot 3.2
* **Batch Processing:** Spring Batch 6.0
* **Data Persistence:** Spring Data JPA, Hibernate, PostgreSQL 16
* **Database Versioning:** Flyway
* **Observability:** Micrometer, Spring Boot Actuator, Prometheus, Grafana
* **Infrastructure:** Docker, Docker Compose
* **Testing:** JUnit 5, Mockito

---

## 🚀 Quickstart (1-Minute Reproduction)

**Prerequisites:** Docker Desktop, Java 21, Maven.

**1. Spin up the Observability & Database Stack**
```bash
docker compose up -d
```

**2. Start the Spring Boot Application**
```bash
mvn spring-boot:run
```

**3. Trigger the Migration via REST**

Open a new terminal and trigger the batch job:
```bash
curl -X POST http://localhost:8080/api/v1/migrations/trigger
```
**4. Watch the Telemetry Live**

* **Grafana Dashboard:** http://localhost:3000 (Login: admin / admin). Import Dashboard ID 4701 to view JVM metrics.
* **Prometheus Targets:** http://localhost:9090/targets

**Database Reset (Optional)**

To instantly truncate the tables and run the benchmark again without restarting Docker:
```bash
docker exec -it migration_db psql -U postgres -d migration_db -c "TRUNCATE TABLE customers, migration_item_error_log RESTART IDENTITY CASCADE;"
```

---

## 📬 Contact & Author
James Russel Batino

Java Software Engineer

https://www.linkedin.com/in/james-batino/

