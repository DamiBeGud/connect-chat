package com.connectchat.presence.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PresenceStatus(
    UUID userId,
    PresenceState status,
    List<PresenceSession> sessions,
    Instant updatedAt
) {}
