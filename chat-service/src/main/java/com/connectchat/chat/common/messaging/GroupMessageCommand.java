package com.connectchat.chat.common.messaging;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GroupMessageCommand(
    UUID messageId,
    UUID groupId,
    UUID senderId,
    List<UUID> recipientIds,
    String content,
    Instant occurredAt
) {}
