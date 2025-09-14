Evently — Spring Boot Backend
Production‑ready Spring Boot 3 backend for event management and bookings with JWT auth, idempotent booking creation, Flyway migrations, PostgreSQL, Caffeine caching, Docker/Compose deployment, Actuator health/readiness, and k6 load tests.

Table of contents
Overview

Features

Architecture

Tech stack

Quick start

Configuration

Build & run

Docker & Compose

Database & migrations

Data seeding

Security (JWT)

Observability (health/logging)

API docs (Swagger)

Load testing (k6)

Trade‑offs and indexes

Deployment notes

License

Overview
Evently exposes REST endpoints to manage events and bookings. It prevents overbooking using transactional updates/locks, makes booking creation safe under retries via Idempotency‑Key, and ships operational endpoints for health/readiness and minimal access logging.

Features
JWT‑secured REST API (Spring Security 6)

Idempotent POST booking with Idempotency‑Key

Duplicate‑prevention and capacity control

PostgreSQL with Flyway migrations

In‑process caching via Caffeine

Actuator health, liveness, readiness

Swagger UI and OpenAPI JSON

Dockerfile and Docker Compose

k6 scripts for smoke/spike/idempotency tests

Architecture
text
flowchart LR
  subgraph Client
    A[Web/Frontend]
    K[k6 Load Tests]
  end
  A -->|HTTPS REST /api/v1/*| G[Spring Boot App]
  K -->|HTTPS REST /api/v1/*| G
  subgraph G[Spring Boot 3 App]
    JF[JwtAuthFilter\n(OncePerRequestFilter)]
    WC[EventController\nBookingController]
    SV[Services\n(EventService, BookingService)]
    RP[Spring Data JPA\nRepositories]
    CF[Caffeine Cache]
    FW[Flyway]
  end
  JF --> WC --> SV --> RP
  SV --> CF
  FW --> DB[(PostgreSQL)]
  RP --> DB
Tech stack
Java 17, Spring Boot 3.x, Spring Security 6

Spring Data JPA, Flyway

PostgreSQL

Caffeine (Spring Cache)

springdoc-openapi (Swagger UI)

Docker, Docker Compose

k6 (load testing)

Quick start
Prerequisites: Java 17, Maven, Docker (optional), PostgreSQL.

Clone repo and set application properties (see Configuration).

Start DB, run migrations, start the app, open Swagger UI.

Configuration
application.properties (local dev):

text
spring.application.name=evently
server.port=8080

# Local PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/evently
spring.datasource.username=postgres
spring.datasource.password=CHANGE_ME
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.open-in-view=false
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=none

# Caffeine cache
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=10000,expireAfterWrite=120s,recordStats

# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.probes.enabled=true
management.endpoint.health.probes.add-additional-paths=true
management.endpoint.health.show-details=never

# Logging (Tomcat access log)
server.tomcat.accesslog.enabled=true
server.tomcat.accesslog.pattern=%t %a "%r" %s (%D ms)

# JWT (example only; replace)
security.jwt.secret=BASE64_ENCODED_256BIT_KEY
security.jwt.expiration-ms=3600000
application-docker.properties (container → host DB):

text
spring.datasource.url=jdbc:postgresql://host.docker.internal:5432/evently
spring.datasource.username=postgres
spring.datasource.password=CHANGE_ME
application-compose.properties (app → db service):

text
spring.datasource.url=jdbc:postgresql://db:5432/evently
spring.datasource.username=postgres
spring.datasource.password=CHANGE_ME
Build & run
Local:

text
mvn clean package
java -jar target/*.jar
With profile:

text
# docker profile (uses host.docker.internal URL)
java -jar target/*.jar --spring.profiles.active=docker
Health checks:

text
curl -fsS http://localhost:8080/actuator/health
curl -fsS http://localhost:8080/actuator/health/liveness
curl -fsS http://localhost:8080/actuator/health/readiness
Docker & Compose
Dockerfile (multi‑stage):

text
# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/*SNAPSHOT.jar app.jar
EXPOSE 8080
# Optional healthcheck (requires wget or curl installed)
# HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
#   CMD wget -q -O - http://localhost:8080/actuator/health/readiness || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
Build/run:

text
docker build -t evently-backend:latest .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=CHANGE_ME \
  evently-backend:latest
docker-compose.yml:

text
version: "3.8"
services:
  db:
    image: postgres:15
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: CHANGE_ME
      POSTGRES_DB: evently
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d evently"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    image: evently-backend:latest
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: compose
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: CHANGE_ME
    ports:
      - "8080:8080"
Compose up:

text
docker compose up -d
Database & migrations
All schema changes are managed via Flyway in db/migration.

Recommended constraints and indexes are included in migrations:

Unique: bookings(idem_key)

Unique: events(name, start_time)

Indexes: bookings(event_id), bookings(user_id)

Example DDL (include via migration):

sql
CREATE UNIQUE INDEX IF NOT EXISTS uq_booking_idem ON bookings(idem_key);
CREATE UNIQUE INDEX IF NOT EXISTS uq_event_name_start ON events(name, start_time);
CREATE INDEX IF NOT EXISTS ix_booking_event ON bookings(event_id);
CREATE INDEX IF NOT EXISTS ix_booking_user ON bookings(user_id);
Data seeding
Option A — Java seeder (profile: demo):

A CommandLineRunner inserts demo users/events idempotently.

Activate with: --spring.profiles.active=demo

Option B — Flyway seed SQL:

Add V2__seed_demo.sql with INSERT … ON CONFLICT DO NOTHING for users/events.

Example demo credentials (if seeded):

admin@example.com / password

user@example.com / password

Security (JWT)
All business endpoints require Authorization: Bearer <JWT>.

Public endpoints:

/v3/api-docs/, /swagger-ui/, /swagger-ui.html

/actuator/health, /actuator/health/liveness, /actuator/health/readiness

The JwtAuthFilter validates tokens and populates SecurityContext.

Observability (health/logging)
Health:

Overall: /actuator/health

Liveness: /actuator/health/liveness

Readiness: /actuator/health/readiness

Minimal request logging:

Tomcat access logs enabled with method, path, status, latency.

API docs (Swagger)
UI: {BASE_URL}/swagger-ui/index.html

OpenAPI JSON: {BASE_URL}/v3/api-docs

Dependency:

text
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.8.13</version>
</dependency>
Load testing (k6)
Project layout suggestion:

text
tests/k6/
  smoke.js
  spike.js
  retry_idempotency.js
Running examples:

text
# Smoke
k6 run -e BASE_URL=http://localhost:8080 tests/k6/smoke.js

# Auth (pass token)
k6 run -e BASE_URL=http://localhost:8080 \
       -e TOKEN="Bearer <JWT>" \
       tests/k6/spike.js

# Idempotency retry
k6 run -e BASE_URL=http://localhost:8080 \
       -e TOKEN="Bearer <JWT>" \
       tests/k6/retry_idempotency.js
Minimal retry_idempotency.js:

javascript
import http from 'k6/http';
import { check } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.1.0/index.js';

export const options = { vus: 1, iterations: 1 };

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN = __ENV.TOKEN || '';

export default function () {
  const idemKey = uuidv4();
  const payload = JSON.stringify({ eventId: 1 }); // adjust
  const headers = { 'Content-Type': 'application/json', 'Idempotency-Key': idemKey };
  if (TOKEN) headers['Authorization'] = TOKEN;

  const r1 = http.post(`${BASE}/api/v1/bookings`, payload, { headers });
  check(r1, { 'first OK': (r) => r.status === 200 || r.status === 201 });

  const r2 = http.post(`${BASE}/api/v1/bookings`, payload, { headers });
  check(r2, {
    'retry same status': (r) => r.status === r1.status,
    'retry same body': (r) => r.body === r1.body,
  });
}
Trade‑offs and indexes
Locking: capacity decrement is enforced inside a transaction using either SELECT … FOR UPDATE or a guarded UPDATE with WHERE remaining > 0; choose the latter to reduce round‑trips and keep transactions short.

Idempotency: Idempotency‑Key ensures safe retries; store key + request hash + response; respond 409 on mismatched payload with same key.

Caching: Caffeine caches bounded with TTL for read‑heavy queries; invalidate on writes that affect cached views.

Indexes: unique(booking.idem_key), unique(event.name,start_time), and FK indexes on booking.event_id and booking.user_id for joins and deletes.

Deployment notes
Local: run via mvn or java -jar.

Docker: connect to host DB via host.docker.internal.

Compose: app connects to db service name db.

Cloud: set environment variables for datasource URL/credentials and expose port 8080; bind a domain and HTTPS; verify /actuator/health and Swagger UI.

