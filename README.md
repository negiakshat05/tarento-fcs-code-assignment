# Warehouse and Store Fulfilment API

Java 17 and Quarkus REST API for managing stores, products, warehouses, and fulfilment associations.

## Features
- Create, retrieve, replace, and archive warehouses.
- Manage stores and products.
- Validate location, capacity, stock, and fulfilment limits.
- Notify the legacy store integration after successful transaction completion.
- Run automated tests with an 80% minimum JaCoCo line-coverage threshold.

## Requirements
- JDK 17+
- Maven
- Docker for Quarkus Dev Services and Testcontainers

## Run locally

```bash
mvn quarkus:dev
```
The API is available at `http://localhost:8080`. With Docker available, Quarkus Dev Services can start PostgreSQL for development and tests.

## Test and coverage

```bash
mvn clean verify
```

The build fails if line coverage is below 80%. The HTML report is generated at `target/site/jacoco/index.html`.

### CI/CD

GitHub Actions automates the Maven build and test execution on configured workflow triggers.

## Assignment documents
- `CODE_ASSIGNMENT.md` — requirements
- `case-study/BRIEFING.md` — domain overview
- `QUESTIONS.md` — written answers
- `case-study/CASE_STUDY.md` — case-study responses
