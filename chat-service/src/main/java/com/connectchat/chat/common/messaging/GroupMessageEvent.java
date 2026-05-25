package com.connectchat.chat.common.messaging;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GroupMessageEvent(
    UUID messageId,
    UUID groupId,
    UUID senderId,
    List<UUID> recipientIds,
    String content,
    Instant occurredAt
) {}
