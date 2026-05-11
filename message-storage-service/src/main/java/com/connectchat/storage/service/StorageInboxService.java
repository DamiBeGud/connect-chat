package com.connectchat.storage.service;

import com.connectchat.storage.common.messaging.PrivateMessageEvent;
import com.connectchat.storage.entity.StorageInboxMessage;
import java.util.List;
import java.util.UUID;

public interface StorageInboxService {
    void enqueue(PrivateMessageEvent event);

    List<StorageInboxMessage> claimNextBatch(int batchSize);

    void markProcessed(UUID id);

    void markFailed(UUID id, String failureReason);
}
