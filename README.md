# Connect Chat

## Run Locally

### Prerequisites

- Java 21
- Docker Desktop

Verify Java:

```bash
java -version
```

### 1. Start local infrastructure

Before starting Docker, make the init scripts executable:

```bash
chmod +x .infra/postgres/init-multiple-dbs.sh
chmod +x .infra/cassandra/create-keyspace.sh
```

The local stack starts:

- PostgreSQL
- pgAdmin
- Redis
- Cassandra
- RabbitMQ

Run:

```bash
docker compose up -d
```

Useful local endpoints:

- PostgreSQL: `localhost:5432`
- pgAdmin: `http://localhost:5050`
- RabbitMQ UI: `http://localhost:15672`
- Redis: `localhost:6379`
- Cassandra: `localhost:9042`

If you already started PostgreSQL before fixing script permissions, recreate the volumes so the database init runs again:

```bash
docker compose down -v
docker compose up -d
```

### 2. Load local environment variables

From the project root:

```bash
set -a
source .env
set +a
```

This enables the `local` Spring profile and provides the local ports and dependency settings for all services.

### 3. Start the services

Each service can be started from its own directory with:

```bash
./gradlew bootRun
```

Example:

```bash
cd identity-service
./gradlew bootRun
```

For hot reload during local development, keep the service running in one terminal and run continuous compilation in another terminal:

```bash
cd identity-service
./gradlew bootRun
```

```bash
cd identity-service
./gradlew --continuous recompileOnChange
```

Spring Boot DevTools will restart the running service whenever Gradle recompiles changed code.

Default local ports:

- `identity-service`: `8081`
- `group-service`: `8082`
- `chat-service`: `8083`
- `message-storage-service`: `8084`
- `presence-service`: `8085`

### Zed Tasks

If you use Zed, project tasks are available to start each service individually or all of them at once from `.zed/tasks.json`.

### Notes

- Local configuration is in `application-local.yaml`.
- Kubernetes/container configuration is in `application-k3s.yaml`.
- If PostgreSQL initialization needs to be recreated, run:

```bash
docker compose down -v
docker compose up -d
```
