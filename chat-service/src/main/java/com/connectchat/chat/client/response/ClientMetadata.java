package com.connectchat.chat.client.response;

public record ClientMetadata(
    String requestId,
    String timestamp,
    int status,
    String message
) {}
