# MediaMix Upload Service

Standalone Spring Boot 2.7.5 application (Java 8) that encapsulates the MediaMix upload domain and schema. It ships with Flyway migrations that create the required `app` schema tables for managing upload sessions, file metadata, and mapping status.

## Prerequisites

- Java 8 JDK (`JAVA_HOME` pointing to JDK 1.8)
- Maven 3.6+
- PostgreSQL 12+ with an accessible database (default jdbc url `jdbc:postgresql://localhost:5432/mediamix`)

## Configuration

Edit `src/main/resources/application.properties` to match your local database credentials. Key properties:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.datasource.hikari.schema` (defaults to `app`)

Flyway is enabled by default and expects the target schema to be `app`. The first application run will execute `V1__init_app_tables.sql` which creates the schema, tables, and indexes.

## Run Locally

```bash
mvn spring-boot:run
```

On startup you should see Flyway apply the migration. Verify tables with:

```sql
\dn app
\dt app.*
```

## Packaging

```bash
mvn clean package
```

The resulting jar is available at `target/mediamix-upload-service-1.0.0-SNAPSHOT.jar`.

## Project Structure

- `src/main/java/com/iri/mktgmix/upload` – Spring Boot application entry point
- `domain` – JPA entities mirroring the MediaMix upload schema
- `repository` – Spring Data JPA repositories
- `src/main/resources/db/migration` – Flyway migrations

Feel free to extend the project with controllers, services, or additional migrations as needed.

