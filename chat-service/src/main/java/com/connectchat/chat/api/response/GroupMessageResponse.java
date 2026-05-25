package com.connectchat.chat.api.response;

import java.time.Instant;
import java.util.UUID;

public record GroupMessageResponse(
    UUID messageId,
    UUID groupId,
    UUID senderId,
    String senderPhoneNumber,
    String content,
    Instant sentAt
) {}
