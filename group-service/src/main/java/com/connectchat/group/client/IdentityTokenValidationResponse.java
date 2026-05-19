package com.connectchat.group.client;

import java.time.Instant;

public record IdentityTokenValidationResponse(
    String subject,
    String tokenType,
    String role,
    Instant expiresAt
) {}
