package com.connectchat.chat.client;

import com.connectchat.chat.client.response.IdentityTokenValidationResponse;

public interface IdentityAuthClient {
    IdentityTokenValidationResponse validateToken(String authorizationHeader);
}
