package com.connectchat.chat.client.response;

import java.time.Instant;
import java.util.UUID;

public record PresenceSessionResponse(
    UUID userId,
    String sessionId,
    String instanceId,
    Instant connectedAt,
    Instant lastHeartbeatAt
) {}
