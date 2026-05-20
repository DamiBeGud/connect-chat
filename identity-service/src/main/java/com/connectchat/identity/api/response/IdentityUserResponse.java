package com.connectchat.identity.api.response;

import java.util.UUID;

public record IdentityUserResponse(UUID userId, String phoneNumber) {}
