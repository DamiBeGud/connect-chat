package com.connectchat.group.api.response;

import java.time.Instant;
import java.util.UUID;

public record GroupResponse(
    UUID id,
    UUID ownerId,
    String name,
    Instant createdAt
) {}
