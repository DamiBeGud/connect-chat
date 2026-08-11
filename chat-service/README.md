# Chat Service

Spring Boot WebSocket service responsible for realtime private and group chat flows.

## Flow Diagrams

Private message delivery with online/offline handling:

![Send private message while recipient may be offline](../.images/Send%20Message%20While%20Recipient%20May%20Be%20Offline.png)

Group message fanout:

![Send group message flow](../.images/Send%20Group%20Message.png)

Offline replay after reconnect:

![Offline replay flow](../.images/Offline%20Replay.png)

Live fanout task processing:

![Live fanout flow](../.images/Live%20Fanout.png)

## Responsibilities

- Authenticate WebSocket sessions through `identity-service`.
- Accept private and group chat messages over STOMP.
- Accept delivered/read acknowledgements.
- Resolve recipients through `identity-service` and group membership through `group-service`.
- Track local WebSocket sessions and coordinate presence with `presence-service`.
- Persist inbox/outbox metadata in PostgreSQL for reliable processing.
- Publish private message, group message, status, bot inbox, and AI reply events through RabbitMQ.
- Deliver available undelivered messages from `message-storage-service`.
- Route messages addressed to the configured AI bot into the AI bot inbox flow.

## Technology

- Java 21
- Spring Boot
- Spring WebSocket/STOMP
- Spring AMQP
- Spring Data JPA
- Spring Data Cassandra
- Flyway
- PostgreSQL
- Cassandra
- RabbitMQ

## Local Port

`8083` by default, controlled by `CHAT_SERVICE_PORT`.

## Dependencies

- PostgreSQL database `chat_service`
- Cassandra keyspace `connect_chat`
- RabbitMQ
- `identity-service`
- `group-service`
- `presence-service`
- `message-storage-service`
- Optional `ai-service` for bot replies

Local defaults are defined in `src/main/resources/application-local.yaml`.

## WebSocket Message Mappings

- `/app/chat.private`
- `/app/chat.group`
- `/app/chat.private.delivered`
- `/app/chat.private.read`
- `/app/chat.group.delivered`
- `/app/chat.group.read`

The exact broker destinations are configured in the WebSocket configuration classes.

## Run Locally

Start shared infrastructure first:

```bash
docker compose up -d postgres cassandra cassandra-init rabbitmq redis
```

Start dependent services as needed, then run:

```bash
cd chat-service
./gradlew bootRun
```

For DevTools reload:

```bash
./gradlew --continuous recompileOnChange
```

## Test

```bash
cd chat-service
./gradlew test
```

There is also a WebSocket private-message script in `tests/websocket-private-message.js` for manual/local checks.

## Design Diagrams

Group message classes and outbox flow:

![Group chat class diagram](../.images/Class%20Diagram%20Group.png)

Offline delivery classes:

![Offline delivery class diagram](../.images/Class%20Diagram.png)

## Notes

- PostgreSQL Flyway migrations live in `src/main/resources/db/migration`.
- Cassandra schema behavior is controlled by `CHAT_CASSANDRA_SCHEMA_ACTION`.
- The service uses inbox/outbox tables to keep message processing idempotent and recoverable.
