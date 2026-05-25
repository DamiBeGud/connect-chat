package com.connectchat.storage.api.response;

import com.connectchat.storage.service.UndeliveredStoredMessage;
import java.time.Instant;
import java.util.UUID;

public record UndeliveredMessageResponse(
    String messageType,
    UUID messageId,
    UUID groupId,
    UUID senderId,
    UUID recipientId,
    String content,
    String status,
    Instant sentAt
) {
    public static UndeliveredMessageResponse from(
        UndeliveredStoredMessage message
    ) {
        return new UndeliveredMessageResponse(
            message.messageType(),
            message.messageId(),
            message.groupId(),
            message.senderId(),
            message.recipientId(),
            message.content(),
            message.status(),
            message.sentAt()
        );
    }
}
