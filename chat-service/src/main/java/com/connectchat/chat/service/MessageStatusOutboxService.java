package com.connectchat.chat.service;

import com.connectchat.chat.common.messaging.PrivateMessageStatus;
import com.connectchat.chat.entity.MessageStatusOutboxEvent;
import java.util.List;
import java.util.UUID;

public interface MessageStatusOutboxService {
    void enqueue(
        UUID messageId,
        UUID senderId,
        UUID recipientId,
        PrivateMessageStatus status,
        UUID actorUserId
    );

    List<MessageStatusOutboxEvent> claimNextBatch(int batchSize);

    void markProcessed(UUID id);

    void markFailed(UUID id, String failureReason);
}
