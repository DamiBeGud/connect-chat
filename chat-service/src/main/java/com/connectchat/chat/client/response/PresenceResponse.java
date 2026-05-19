package com.connectchat.chat.client.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PresenceResponse(
    UUID userId,
    String status,
    List<PresenceSessionResponse> sessions,
    Instant updatedAt
) {}
