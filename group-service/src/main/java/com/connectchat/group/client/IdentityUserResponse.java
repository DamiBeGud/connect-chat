package com.connectchat.group.client;

import java.util.UUID;

public record IdentityUserResponse(
    UUID userId,
    String phoneNumber,
    String firstName,
    String lastName,
    String nickname
) {}
