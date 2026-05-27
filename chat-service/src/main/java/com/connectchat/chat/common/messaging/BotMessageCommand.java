package com.connectchat.chat.common.messaging;

import java.time.Instant;
import java.util.UUID;

public record BotMessageCommand(
    UUID messageId,
    UUID senderId,
    UUID botUserId,
    String content,
    Instant occurredAt
) {}
