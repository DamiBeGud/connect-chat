package com.connectchat.identity.api.response;

public record AuthTokenResponse(String accessToken, String refreshToken) {}
