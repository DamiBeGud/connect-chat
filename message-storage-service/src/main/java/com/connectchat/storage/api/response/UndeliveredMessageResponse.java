package com.connectchat.storage.api.response;

import com.connectchat.storage.entity.UndeliveredMessage;
import java.time.Instant;
import java.util.UUID;

public record UndeliveredMessageResponse(
    UUID messageId,
    UUID senderId,
    UUID recipientId,
    String content,
    String status,
    Instant sentAt
) {
    public static UndeliveredMessageResponse from(UndeliveredMessage message) {
        return new UndeliveredMessageResponse(
            message.messageId(),
            message.getSenderId(),
            message.recipientId(),
            message.getContent(),
            message.getStatus(),
            message.sentAt()
        );
    }
}
