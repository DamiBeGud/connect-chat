package com.connectchat.group.client;

public interface IdentityAuthClient {
    IdentityTokenValidationResponse validateToken(String bearerToken);
}
