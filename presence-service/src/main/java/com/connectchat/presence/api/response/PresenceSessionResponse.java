package com.connectchat.presence.api.response;

import java.time.Instant;
import java.util.UUID;

public record PresenceSessionResponse(
    UUID userId,
    String sessionId,
    String instanceId,
    Instant connectedAt,
    Instant lastHeartbeatAt
) {}
