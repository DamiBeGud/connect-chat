package com.connectchat.chat.common.messaging;

import java.time.Instant;
import java.util.UUID;

public record PrivateMessageEvent(
    UUID messageId,
    UUID senderId,
    UUID recipientId,
    String content,
    Instant occurredAt
) {}
