# Group Service

Spring Boot service responsible for chat group membership and group-level authorization.

## Diagram

![Group service class diagram](../.images/Screenshot%202026-05-18%20at%2016.29.23.png)

## Responsibilities

- Create groups.
- Add members to groups.
- Remove members from groups.
- Let the current caller leave a group.
- List group members.
- Return group member IDs for chat fanout.
- Validate caller identity through `identity-service`.
- Resolve users through `identity-service` when membership operations need user metadata.

## Technology

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Flyway
- PostgreSQL

## Local Port

`8082` by default, controlled by `GROUP_SERVICE_PORT`.

## Dependencies

- PostgreSQL database `group_service`
- `identity-service` for token validation and user lookup

Local defaults are defined in `src/main/resources/application-local.yaml`.

## Key Endpoints

- `POST /api/v1/groups`
- `POST /api/v1/groups/{groupId}/members`
- `DELETE /api/v1/groups/{groupId}/members/{userId}`
- `DELETE /api/v1/groups/{groupId}/members/me`
- `GET /api/v1/groups/{groupId}/members`
- `GET /api/v1/groups/{groupId}/member-ids`

Requests are authenticated through identity-service-backed security filters.

## Run Locally

Start shared infrastructure from the repository root:

```bash
docker compose up -d postgres
```

Start `identity-service` in a separate terminal if it is not already running:

```bash
cd identity-service
./gradlew bootRun
```

Then run this service:

```bash
cd group-service
./gradlew bootRun
```

For DevTools reload:

```bash
./gradlew --continuous recompileOnChange
```

## Test

```bash
cd group-service
./gradlew test
```

## Notes

- Flyway migrations live in `src/main/resources/db/migration`.
- Group authorization is enforced in the application service layer, not only at the controller boundary.
