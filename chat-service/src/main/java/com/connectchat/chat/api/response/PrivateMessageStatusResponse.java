package com.connectchat.chat.api.response;

import com.connectchat.chat.common.messaging.PrivateMessageStatus;
import java.time.Instant;
import java.util.UUID;

public record PrivateMessageStatusResponse(
    UUID messageId,
    UUID senderId,
    UUID recipientId,
    PrivateMessageStatus status,
    UUID actorUserId,
    Instant occurredAt
) {}
