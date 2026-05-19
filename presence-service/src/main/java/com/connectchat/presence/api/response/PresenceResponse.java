package com.connectchat.presence.api.response;

import com.connectchat.presence.entity.PresenceState;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PresenceResponse(
    UUID userId,
    PresenceState status,
    List<PresenceSessionResponse> sessions,
    Instant updatedAt
) {}
