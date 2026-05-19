package com.connectchat.identity.api.response;

import java.time.Instant;

public record AuthTokenValidationResponse(
    String subject,
    String tokenType,
    String role,
    Instant expiresAt
) {}
