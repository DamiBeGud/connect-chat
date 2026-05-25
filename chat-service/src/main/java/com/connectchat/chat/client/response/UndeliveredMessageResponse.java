package com.connectchat.chat.client.response;

import java.time.Instant;
import java.util.UUID;

public record UndeliveredMessageResponse(
    String messageType,
    UUID messageId,
    UUID groupId,
    UUID senderId,
    UUID recipientId,
    String content,
    String status,
    Instant sentAt
) {}
