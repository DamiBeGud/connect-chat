# Message Storage Service

Spring Boot service responsible for durable message storage and undelivered-message projections.

## Diagrams

Storage and offline delivery classes:

![Message storage class diagram](../.images/Class%20Diagram.png)

Delivered acknowledgement cleanup:

![Delivered acknowledgement cleanup](../.images/Delivered%20Ack%20Removes%20Message%20From%20Offline%20Queue.png)

Group acknowledgement cleanup:

![Group acknowledgement cleanup](../.images/Group%20Acknowledgement%20Cleanup.png)

## Responsibilities

- Consume private message events from RabbitMQ.
- Consume group message events from RabbitMQ.
- Consume message status events from RabbitMQ.
- Store private and group messages in Cassandra.
- Maintain undelivered-message views for offline delivery.
- Track recipient status for group messages.
- Publish confirmed status events after storage state changes.
- Expose read APIs used by `chat-service` when a user reconnects.

## Technology

- Java 21
- Spring Boot
- Spring AMQP
- Spring Data Cassandra
- Spring Data JPA
- Flyway
- PostgreSQL
- Cassandra
- RabbitMQ

## Local Port

`8084` by default, controlled by `MESSAGE_STORAGE_SERVICE_PORT`.

## Dependencies

- PostgreSQL database `message_storage_service`
- Cassandra keyspace `connect_chat`
- RabbitMQ

Local defaults are defined in `src/main/resources/application-local.yaml`.

## Key Endpoints

- `GET /api/v1/messages/users/{userId}/undelivered?limit=50`
- `POST /api/v1/messages/group/{messageId}/recipients/{recipientId}/status`

Most write activity enters through RabbitMQ consumers rather than HTTP endpoints.

## Run Locally

Start shared infrastructure first:

```bash
docker compose up -d postgres cassandra cassandra-init rabbitmq
```

Then run the service:

```bash
cd message-storage-service
./gradlew bootRun
```

## Test

```bash
cd message-storage-service
./gradlew test
```

## Notes

- PostgreSQL Flyway migrations live in `src/main/resources/db/migration`.
- Cassandra schema initialization is handled by the service code and Cassandra configuration.
- Storage inbox tables make RabbitMQ event handling idempotent.
