package com.connectchat.presence.client;

public interface IdentityAuthClient {
    IdentityTokenValidationResponse validateToken(String bearerToken);
}
