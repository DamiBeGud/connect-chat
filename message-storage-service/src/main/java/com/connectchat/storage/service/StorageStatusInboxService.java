package com.connectchat.storage.service;

import com.connectchat.storage.common.messaging.MessageStatusEvent;
import com.connectchat.storage.entity.StorageStatusInboxMessage;
import java.util.List;
import java.util.UUID;

public interface StorageStatusInboxService {
    void enqueue(MessageStatusEvent event);

    List<StorageStatusInboxMessage> claimNextBatch(int batchSize);

    void markPending(UUID id);

    void markProcessed(UUID id);

    void markFailed(UUID id, String failureReason);
}
