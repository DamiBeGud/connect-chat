package com.connectchat.chat.client.response;

import java.time.Instant;
import java.util.UUID;

public record UndeliveredMessageResponse(
    UUID messageId,
    UUID senderId,
    UUID recipientId,
    String content,
    String status,
    Instant sentAt
) {}
