package com.connectchat.chat.client.response;

import java.time.Instant;

public record IdentityTokenValidationResponse(
    String subject,
    String tokenType,
    String role,
    Instant expiresAt
) {}
