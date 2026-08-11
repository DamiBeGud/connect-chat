# Identity Service

Spring Boot service responsible for user identity and authentication.

## Diagram

![Identity service class diagram](../.images/Screenshot%202026-05-05%20at%2019.58.04.png)

## Responsibilities

- Register users by phone number.
- Verify registration codes and issue auth tokens.
- Refresh access tokens.
- Issue short-lived internal service tokens for trusted service-to-service calls.
- Validate JWTs for downstream services.
- Expose internal user lookup by user ID or phone number.
- Seed the default AI bot user used by `ai-service`.

## Technology

- Java 21
- Spring Boot
- Spring Security OAuth2 Resource Server
- Spring Data JPA
- Flyway
- PostgreSQL
- Twilio SDK for SMS delivery

## Local Port

`8081` by default, controlled by `IDENTITY_SERVICE_PORT`.

## Dependencies

- PostgreSQL database `identity_service`
- Optional Twilio credentials for real SMS delivery

Local defaults are defined in `src/main/resources/application-local.yaml`.

## Key Endpoints

- `GET /api/v1/identity`
- `POST /api/v1/identity/auth/register`
- `POST /api/v1/identity/auth/register/verify`
- `POST /api/v1/identity/auth/token/refresh`
- `POST /api/v1/identity/auth/service-token`
- `POST /api/v1/identity/auth/token/validate`
- `GET /api/v1/identity/users/{userId}`
- `GET /api/v1/identity/users/by-phone/{phoneNumber}`

User lookup endpoints require an internal service token.

## Run Locally

Start shared infrastructure from the repository root:

```bash
docker compose up -d postgres
```

Then run the service:

```bash
cd identity-service
./gradlew bootRun
```

For DevTools reload:

```bash
./gradlew --continuous recompileOnChange
```

## Test

```bash
cd identity-service
./gradlew test
```

## Notes

- Flyway migrations live in `src/main/resources/db/migration`.
- `V7__seed_ai_bot_user.sql` creates the default AI bot identity.
- Local JWT and client secrets are development-only defaults and should not be reused outside local environments.
