package com.connectchat.chat.client;

import com.connectchat.chat.client.response.IdentityUserResponse;
import java.util.UUID;

public interface IdentityUserClient {
    IdentityUserResponse getUserById(UUID userId);

    IdentityUserResponse getUserByPhoneNumber(String phoneNumber);
}
