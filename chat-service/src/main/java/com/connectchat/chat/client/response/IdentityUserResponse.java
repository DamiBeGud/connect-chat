package com.connectchat.chat.client.response;

import java.util.UUID;

public record IdentityUserResponse(UUID userId, String phoneNumber) {}
