package com.connectchat.chat.common.messaging;

import java.time.Instant;
import java.util.UUID;

public record MessageStatusEvent(
    UUID eventId,
    UUID messageId,
    UUID senderId,
    UUID recipientId,
    PrivateMessageStatus status,
    UUID actorUserId,
    Instant occurredAt
) {}
