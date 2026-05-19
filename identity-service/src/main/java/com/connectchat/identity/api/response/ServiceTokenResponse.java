package com.connectchat.identity.api.response;

import java.time.Instant;

public record ServiceTokenResponse(
    String accessToken,
    String tokenType,
    String role,
    Instant expiresAt
) {}
