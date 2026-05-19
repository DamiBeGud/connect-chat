package com.connectchat.chat.client.response;

import java.time.Instant;

public record ServiceTokenResponse(
    String accessToken,
    String tokenType,
    String role,
    Instant expiresAt
) {}
