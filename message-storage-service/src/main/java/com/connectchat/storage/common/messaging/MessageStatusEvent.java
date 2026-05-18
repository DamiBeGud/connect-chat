package com.connectchat.storage.common.messaging;

import com.connectchat.storage.entity.StoredMessageStatus;
import java.time.Instant;
import java.util.UUID;

public record MessageStatusEvent(
    UUID eventId,
    UUID messageId,
    UUID senderId,
    UUID recipientId,
    StoredMessageStatus status,
    UUID actorUserId,
    Instant occurredAt
) {}
