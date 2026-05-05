package com.connectchat.identity.common.web;

public record Metadata(
    String requestId,
    String timestamp,
    int status,
    String message
) {}
