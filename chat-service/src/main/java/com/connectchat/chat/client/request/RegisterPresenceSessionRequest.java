package com.connectchat.chat.client.request;

import java.util.UUID;

public record RegisterPresenceSessionRequest(
    UUID userId,
    String sessionId,
    String instanceId
) {}
