package com.connectchat.storage.service;

import java.time.Instant;
import java.util.UUID;

public record UndeliveredStoredMessage(
    String messageType,
    UUID messageId,
    UUID groupId,
    UUID senderId,
    UUID recipientId,
    String content,
    String status,
    Instant sentAt
) {}
