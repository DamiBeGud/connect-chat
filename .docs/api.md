# Connect Chat API Documentation

This document describes the current REST and WebSocket APIs for building a mobile client.

Local service URLs:

| Service | Local URL |
| --- | --- |
| Identity Service | `http://localhost:8081` |
| Group Service | `http://localhost:8082` |
| Chat Service WebSocket | `ws://localhost:8083/ws/chat` |
| Message Storage Service | `http://localhost:8084` |
| Presence Service | `http://localhost:8085` |

## Common Conventions

All REST services that return JSON use this envelope:

```json
{
  "metadata": {
    "requestId": "request/session id",
    "timestamp": "2026-05-19T13:25:19.952135Z",
    "status": 200,
    "message": "Human-readable result"
  },
  "data": {},
  "error": null
}
```

Error response shape:

```json
{
  "metadata": {
    "requestId": "request/session id",
    "timestamp": "2026-05-19T13:25:19.952135Z",
    "status": 400,
    "message": "Bad Request"
  },
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed"
  }
}
```

Use ISO-8601 timestamps. UUID fields are strings.

Authenticated user APIs require:

```http
Authorization: Bearer <user-access-token>
```

Internal APIs require:

```http
Authorization: Bearer <internal-service-token>
```

Mobile clients should use only user access tokens. Internal service tokens are for backend services only.

## Identity Service

Base URL:

```text
http://localhost:8081/api/v1/identity
```

### Register

Starts registration and sends a verification code by SMS.

```http
POST /api/v1/identity/auth/register
Content-Type: application/json
```

Request:

```json
{
  "phoneNumber": "+15551234567",
  "firstName": "Dami",
  "lastName": "Begud",
  "nickname": "dami",
  "dateOfBirth": "1995-04-20",
  "country": "US"
}
```

Validation:

| Field | Required | Rule |
| --- | --- | --- |
| `phoneNumber` | yes | E.164 format, example `+15551234567` |
| `firstName` | yes | max 100 chars |
| `lastName` | yes | max 100 chars |
| `nickname` | no | max 100 chars |
| `dateOfBirth` | yes | date in the past, format `YYYY-MM-DD` |
| `country` | yes | max 100 chars |

Response:

```json
{
  "metadata": {
    "status": 200,
    "message": "Registration verification code sent"
  },
  "data": null,
  "error": null
}
```

Example:

```bash
curl -X POST "http://localhost:8081/api/v1/identity/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+15551234567",
    "firstName": "Dami",
    "lastName": "Begud",
    "nickname": "dami",
    "dateOfBirth": "1995-04-20",
    "country": "US"
  }'
```

### Verify Registration

Verifies the SMS code and returns user tokens.

```http
POST /api/v1/identity/auth/register/verify
Content-Type: application/json
```

Request:

```json
{
  "phoneNumber": "+15551234567",
  "verificationCode": "123456"
}
```

Validation:

| Field | Required | Rule |
| --- | --- | --- |
| `phoneNumber` | yes | E.164 format |
| `verificationCode` | yes | exactly 6 digits |

Response:

```json
{
  "metadata": {
    "status": 200,
    "message": "Registration verified"
  },
  "data": {
    "accessToken": "jwt-access-token",
    "refreshToken": "uuid-refresh-token"
  },
  "error": null
}
```

Example:

```bash
curl -X POST "http://localhost:8081/api/v1/identity/auth/register/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+15551234567",
    "verificationCode": "123456"
  }'
```

### Refresh Token

Returns a new access token and refresh token.

```http
POST /api/v1/identity/auth/token/refresh
Content-Type: application/json
```

Request:

```json
{
  "refreshToken": "uuid-refresh-token"
}
```

Response:

```json
{
  "metadata": {
    "status": 200,
    "message": "Token refreshed"
  },
  "data": {
    "accessToken": "new-jwt-access-token",
    "refreshToken": "new-uuid-refresh-token"
  },
  "error": null
}
```

### Validate Token

Validates a bearer token and returns token identity. This is mainly for backend services.

```http
POST /api/v1/identity/auth/token/validate
Authorization: Bearer <token>
```

Response:

```json
{
  "metadata": {
    "status": 200,
    "message": "Token is valid"
  },
  "data": {
    "subject": "user-id-or-service-id",
    "tokenType": "user",
    "role": "USER",
    "expiresAt": "2026-05-19T18:30:00Z"
  },
  "error": null
}
```

User access tokens contain:

```json
{
  "sub": "user-uuid",
  "token_type": "user",
  "role": "USER"
}
```

The mobile app can decode the access token locally to read the current user id from `sub`, but authorization should still rely on backend validation.

### Issue Internal Service Token

Internal only. Mobile clients should not call this endpoint.

```http
POST /api/v1/identity/auth/service-token
Content-Type: application/json
```

Request:

```json
{
  "clientId": "chat-service",
  "clientSecret": "local-chat-service-secret"
}
```

Response:

```json
{
  "metadata": {
    "status": 200,
    "message": "Service token issued"
  },
  "data": {
    "accessToken": "jwt-service-token",
    "tokenType": "service",
    "role": "INTERNAL_SERVICE",
    "expiresAt": "2026-05-19T18:30:00Z"
  },
  "error": null
}
```

### Identity Smoke Check

```http
GET /api/v1/identity
```

Returns `200 OK` with empty body.

## Group Service

Base URL:

```text
http://localhost:8082/api/v1/groups
```

All group APIs require a user access token unless explicitly called by an internal service.

### Create Group

```http
POST /api/v1/groups
Authorization: Bearer <user-access-token>
Content-Type: application/json
```

Request:

```json
{
  "name": "Best Friends"
}
```

Validation:

| Field | Required | Rule |
| --- | --- | --- |
| `name` | yes | not blank, max 120 chars |

Response:

```json
{
  "metadata": {
    "status": 201,
    "message": "Group created"
  },
  "data": {
    "id": "9d46599f-27cb-4d2b-8d3b-4fffce6773d3",
    "ownerId": "793de6b4-7ced-4a80-80c7-dd22d9b90a72",
    "name": "Best Friends",
    "createdAt": "2026-05-19T13:25:19.952135Z"
  },
  "error": null
}
```

Example:

```bash
curl -X POST "http://localhost:8082/api/v1/groups" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "name": "Best Friends" }'
```

### Add Group Member

Only the group owner can add members.

```http
POST /api/v1/groups/{groupId}/members
Authorization: Bearer <owner-access-token>
Content-Type: application/json
```

Request:

```json
{
  "userId": "ac9b3a0a-bedb-45bc-975c-9a3b83a6ca09"
}
```

Response:

```json
{
  "metadata": {
    "status": 201,
    "message": "Group member added"
  },
  "data": {
    "groupId": "9d46599f-27cb-4d2b-8d3b-4fffce6773d3",
    "userId": "ac9b3a0a-bedb-45bc-975c-9a3b83a6ca09",
    "role": "MEMBER",
    "joinedAt": "2026-05-19T13:25:19.952135Z"
  },
  "error": null
}
```

### Remove Group Member

Only the group owner can remove another member. The owner cannot remove themself.

```http
DELETE /api/v1/groups/{groupId}/members/{userId}
Authorization: Bearer <owner-access-token>
```

Response:

```json
{
  "metadata": {
    "status": 200,
    "message": "Group member removed"
  },
  "data": null,
  "error": null
}
```

### Leave Group

A member can remove themself from a group. The owner cannot leave through this endpoint until ownership transfer exists.

```http
DELETE /api/v1/groups/{groupId}/members/me
Authorization: Bearer <user-access-token>
```

Response:

```json
{
  "metadata": {
    "status": 200,
    "message": "Left group"
  },
  "data": null,
  "error": null
}
```

### Get Group Members

Available to group members and internal services.

```http
GET /api/v1/groups/{groupId}/members
Authorization: Bearer <user-access-token>
```

Response:

```json
{
  "metadata": {
    "status": 200,
    "message": "Group members fetched"
  },
  "data": [
    {
      "groupId": "9d46599f-27cb-4d2b-8d3b-4fffce6773d3",
      "userId": "793de6b4-7ced-4a80-80c7-dd22d9b90a72",
      "role": "OWNER",
      "joinedAt": "2026-05-19T13:25:19.952135Z"
    },
    {
      "groupId": "9d46599f-27cb-4d2b-8d3b-4fffce6773d3",
      "userId": "ac9b3a0a-bedb-45bc-975c-9a3b83a6ca09",
      "role": "MEMBER",
      "joinedAt": "2026-05-19T13:26:00.000000Z"
    }
  ],
  "error": null
}
```

## Chat WebSocket API

Chat uses raw WebSocket with STOMP.

Endpoint:

```text
ws://localhost:8083/ws/chat
```

STOMP app destination prefix:

```text
/app
```

STOMP user destination prefix:

```text
/user
```

Connect with user access token:

```text
CONNECT
Authorization: Bearer <user-access-token>
accept-version:1.2,1.1,1.0
heart-beat:10000,10000
```

Chat-service validates this token through identity-service during `CONNECT`. The token must be a user token with role `USER`.

### Required Subscriptions

Subscribe to private messages:

```text
/user/queue/private-messages
```

Payload received:

```json
{
  "messageId": "42fa8a65-a118-49c9-bd50-0ff96116d0e8",
  "senderId": "793de6b4-7ced-4a80-80c7-dd22d9b90a72",
  "recipientId": "ac9b3a0a-bedb-45bc-975c-9a3b83a6ca09",
  "content": "Hello World",
  "sentAt": "2026-05-19T13:30:00.000000Z"
}
```

Subscribe to private message status updates:

```text
/user/queue/private-message-status
```

Payload received:

```json
{
  "messageId": "42fa8a65-a118-49c9-bd50-0ff96116d0e8",
  "senderId": "793de6b4-7ced-4a80-80c7-dd22d9b90a72",
  "recipientId": "ac9b3a0a-bedb-45bc-975c-9a3b83a6ca09",
  "status": "DELIVERED",
  "actorUserId": "ac9b3a0a-bedb-45bc-975c-9a3b83a6ca09",
  "occurredAt": "2026-05-19T13:30:05.000000Z"
}
```

Status values:

| Status | Meaning |
| --- | --- |
| `SENT` | Message was stored successfully by message-storage-service. |
| `DELIVERED` | Recipient acknowledged receiving the message. |
| `READ` | Recipient acknowledged reading the message. |

### Send Private Message

Destination:

```text
/app/chat.private
```

Body:

```json
{
  "recipientId": "ac9b3a0a-bedb-45bc-975c-9a3b83a6ca09",
  "content": "Hello World"
}
```

Validation:

| Field | Required | Rule |
| --- | --- | --- |
| `recipientId` | yes | UUID |
| `content` | yes | not blank, max 4000 chars |

Notes:

- The sender is resolved from the WebSocket access token.
- The message is eventually delivered to both sender and recipient on `/user/queue/private-messages`.
- The message is also stored asynchronously by message-storage-service.

### Acknowledge Delivered

Only the recipient can acknowledge `DELIVERED`.

Destination:

```text
/app/chat.private.delivered
```

Body:

```json
{
  "messageId": "42fa8a65-a118-49c9-bd50-0ff96116d0e8"
}
```

### Acknowledge Read

Only the recipient can acknowledge `READ`.

Destination:

```text
/app/chat.private.read
```

Body:

```json
{
  "messageId": "42fa8a65-a118-49c9-bd50-0ff96116d0e8"
}
```

### STOMP JavaScript Example

This is a minimal browser-compatible example. Mobile clients should use the equivalent STOMP client for their platform.

```js
import { Client } from "@stomp/stompjs";

const accessToken = "user-access-token";

const client = new Client({
  brokerURL: "ws://localhost:8083/ws/chat",
  connectHeaders: {
    Authorization: `Bearer ${accessToken}`,
  },
  onConnect: () => {
    client.subscribe("/user/queue/private-messages", (frame) => {
      const message = JSON.parse(frame.body);
      console.log("message", message);

      client.publish({
        destination: "/app/chat.private.delivered",
        body: JSON.stringify({ messageId: message.messageId }),
        headers: { "content-type": "application/json" },
      });
    });

    client.subscribe("/user/queue/private-message-status", (frame) => {
      console.log("status", JSON.parse(frame.body));
    });
  },
});

client.activate();

function sendMessage(recipientId, content) {
  client.publish({
    destination: "/app/chat.private",
    body: JSON.stringify({ recipientId, content }),
    headers: { "content-type": "application/json" },
  });
}

function markRead(messageId) {
  client.publish({
    destination: "/app/chat.private.read",
    body: JSON.stringify({ messageId }),
    headers: { "content-type": "application/json" },
  });
}
```

## Presence Service

Presence-service is internal only. Mobile clients should not call it directly. Chat-service calls it with an internal service token.

Base URL:

```text
http://localhost:8085/api/v1/presence
```

### Register Session

Internal only.

```http
POST /api/v1/presence/sessions
Authorization: Bearer <internal-service-token>
Content-Type: application/json
```

Request:

```json
{
  "userId": "793de6b4-7ced-4a80-80c7-dd22d9b90a72",
  "sessionId": "websocket-session-id",
  "instanceId": "chat-service:local-chat-service-1"
}
```

Response:

```json
{
  "metadata": {
    "status": 201,
    "message": "Presence session registered"
  },
  "data": null,
  "error": null
}
```

### Remove Session

Internal only.

```http
DELETE /api/v1/presence/sessions/{sessionId}
Authorization: Bearer <internal-service-token>
```

Response:

```json
{
  "metadata": {
    "status": 200,
    "message": "Presence session removed"
  },
  "data": null,
  "error": null
}
```

### Get User Presence

Internal only.

```http
GET /api/v1/presence/users/{userId}
Authorization: Bearer <internal-service-token>
```

Response:

```json
{
  "metadata": {
    "status": 200,
    "message": "Presence fetched"
  },
  "data": {
    "userId": "793de6b4-7ced-4a80-80c7-dd22d9b90a72",
    "status": "ONLINE",
    "sessions": [
      {
        "userId": "793de6b4-7ced-4a80-80c7-dd22d9b90a72",
        "sessionId": "websocket-session-id",
        "instanceId": "chat-service:local-chat-service-1",
        "connectedAt": "2026-05-19T13:30:00.000000Z",
        "lastHeartbeatAt": "2026-05-19T13:30:00.000000Z"
      }
    ],
    "updatedAt": "2026-05-19T13:30:00.000000Z"
  },
  "error": null
}
```

Presence statuses:

```text
ONLINE
OFFLINE
```

Current implementation does not have heartbeat refresh yet. `lastHeartbeatAt` is set when the session is registered.

### Check If User Is Online

Internal only.

```http
GET /api/v1/presence/users/{userId}/online
Authorization: Bearer <internal-service-token>
```

Response:

```json
{
  "metadata": {
    "status": 200,
    "message": "Presence status fetched"
  },
  "data": {
    "userId": "793de6b4-7ced-4a80-80c7-dd22d9b90a72",
    "online": true
  },
  "error": null
}
```

### Batch Presence Lookup

Internal only.

```http
POST /api/v1/presence/users/lookup
Authorization: Bearer <internal-service-token>
Content-Type: application/json
```

Request:

```json
{
  "userIds": [
    "793de6b4-7ced-4a80-80c7-dd22d9b90a72",
    "ac9b3a0a-bedb-45bc-975c-9a3b83a6ca09"
  ]
}
```

Response:

```json
{
  "metadata": {
    "status": 200,
    "message": "Presence lookup fetched"
  },
  "data": [
    {
      "userId": "793de6b4-7ced-4a80-80c7-dd22d9b90a72",
      "status": "ONLINE",
      "sessions": [
        {
          "userId": "793de6b4-7ced-4a80-80c7-dd22d9b90a72",
          "sessionId": "websocket-session-id",
          "instanceId": "chat-service:local-chat-service-1",
          "connectedAt": "2026-05-19T13:30:00.000000Z",
          "lastHeartbeatAt": "2026-05-19T13:30:00.000000Z"
        }
      ],
      "updatedAt": "2026-05-19T13:30:00.000000Z"
    },
    {
      "userId": "ac9b3a0a-bedb-45bc-975c-9a3b83a6ca09",
      "status": "OFFLINE",
      "sessions": [],
      "updatedAt": "2026-05-19T13:31:00.000000Z"
    }
  ],
  "error": null
}
```

## Message Storage Service

Message-storage-service currently has no mobile-facing REST API.

It consumes RabbitMQ events from chat-service and stores private messages/status updates. Mobile clients receive messages and statuses through chat-service WebSocket subscriptions.

## Health Endpoints

Each service exposes health:

```http
GET /actuator/health
```

Examples:

```bash
curl "http://localhost:8081/actuator/health"
curl "http://localhost:8082/actuator/health"
curl "http://localhost:8083/actuator/health"
curl "http://localhost:8084/actuator/health"
curl "http://localhost:8085/actuator/health"
```

## Recommended Mobile App Flow

1. Register user with `POST /identity/auth/register`.
2. Verify registration with `POST /identity/auth/register/verify`.
3. Store `accessToken` and `refreshToken` securely.
4. Use `accessToken` for group REST APIs.
5. Connect to chat WebSocket with `Authorization: Bearer <accessToken>`.
6. Subscribe to `/user/queue/private-messages`.
7. Subscribe to `/user/queue/private-message-status`.
8. Send private messages to `/app/chat.private`.
9. When receiving a message as recipient, publish delivered/read acknowledgements.
10. Refresh tokens with `POST /identity/auth/token/refresh` before or after access token expiration.
