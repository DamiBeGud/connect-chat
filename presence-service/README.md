# Presence Service

Spring Boot service responsible for online state and active presence sessions.

## Diagrams

Presence service model:

![Presence service class diagram](../.images/Screenshot%202026-05-19%20at%2015.53.40.png)


## Responsibilities

- Register active client sessions.
- Remove sessions when clients disconnect.
- Report whether a user is online.
- Return detailed presence for one or more users.
- Store ephemeral presence data in Redis.
- Validate service callers through `identity-service`.

## Technology

- Java 21
- Spring Boot
- Spring Security
- Spring Data Redis
- Redis

## Local Port

`8085` by default, controlled by `PRESENCE_SERVICE_PORT`.

## Dependencies

- Redis
- `identity-service` for token validation

Local defaults are defined in `src/main/resources/application-local.yaml`.

## Key Endpoints

- `POST /api/v1/presence/sessions`
- `DELETE /api/v1/presence/sessions/{sessionId}`
- `GET /api/v1/presence/users/{userId}`
- `GET /api/v1/presence/users/{userId}/online`
- `POST /api/v1/presence/users/lookup`

These endpoints are primarily used by `chat-service` to coordinate WebSocket lifecycle state.

## Run Locally

Start Redis from the repository root:

```bash
docker compose up -d redis
```

Start `identity-service` in a separate terminal if it is not already running:

```bash
cd identity-service
./gradlew bootRun
```

Then run this service:

```bash
cd presence-service
./gradlew bootRun
```

For DevTools reload:

```bash
./gradlew --continuous recompileOnChange
```

## Test

```bash
cd presence-service
./gradlew test
```

## Notes

- Presence state is intentionally ephemeral.
- Session expiration and cleanup behavior is configured through presence session properties.
