package com.connectchat.group.common.web;

public record Metadata(
    String requestId,
    String timestamp,
    int status,
    String message
) {}
