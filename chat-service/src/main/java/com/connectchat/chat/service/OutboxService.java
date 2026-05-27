package com.connectchat.chat.service;

import com.connectchat.chat.entity.OutboxMessage;
import java.util.List;
import java.util.UUID;

public interface OutboxService {
    OutboxMessage enqueuePrivateMessage(
        UUID senderId,
        UUID recipientId,
        String content
    );

    OutboxMessage requireMessage(UUID messageId);

    List<OutboxMessage> claimNextBatch(int batchSize);

    void markProcessed(UUID id);

    void markFailed(UUID id, String failureReason);
}
