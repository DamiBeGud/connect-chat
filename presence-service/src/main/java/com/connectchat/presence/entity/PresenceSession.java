package com.connectchat.presence.entity;

import java.time.Instant;
import java.util.UUID;

public record PresenceSession(
    UUID userId,
    String sessionId,
    String instanceId,
    Instant connectedAt,
    Instant lastHeartbeatAt
) {}
