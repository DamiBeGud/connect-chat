package com.connectchat.chat.api.response;

import java.time.Instant;
import java.util.UUID;

public record PrivateMessageResponse(
    UUID messageId,
    UUID senderId,
    String senderPhoneNumber,
    UUID recipientId,
    String content,
    Instant sentAt
) {}
