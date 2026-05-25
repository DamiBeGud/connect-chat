package com.connectchat.group.client;

import java.time.Instant;

public record ServiceTokenResponse(
    String accessToken,
    String tokenType,
    String role,
    Instant expiresAt
) {}
