package com.connectchat.chat.service;

import com.connectchat.chat.entity.GroupOutboxMessage;
import java.util.List;
import java.util.UUID;

public interface GroupOutboxService {
    void enqueueGroupMessage(
        UUID groupId,
        UUID senderId,
        List<UUID> recipientIds,
        String content
    );

    GroupOutboxMessage requireMessage(UUID messageId);

    void requireRecipient(UUID messageId, UUID recipientId);

    List<UUID> recipientIds(UUID messageId);

    List<GroupOutboxMessage> claimNextBatch(int batchSize);

    void markProcessed(UUID id);

    void markFailed(UUID id, String failureReason);
}
