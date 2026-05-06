package com.connectchat.chat.api.response;

import java.time.Instant;
import java.util.UUID;

public record PrivateMessageResponse(
    UUID messageId,
    UUID senderId,
    UUID recipientId,
    String content,
    Instant sentAt
) {}
