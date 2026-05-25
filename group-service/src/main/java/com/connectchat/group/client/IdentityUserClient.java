package com.connectchat.group.client;

import java.util.UUID;

public interface IdentityUserClient {
    IdentityUserResponse getUserById(UUID userId);

    IdentityUserResponse getUserByPhoneNumber(String phoneNumber);
}
