package com.connectchat.identity.util;

public record Metadata(
    String requestId,
    String timestamp,
    int status,
    String message
) {}
