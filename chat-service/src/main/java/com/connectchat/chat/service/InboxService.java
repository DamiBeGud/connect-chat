package com.connectchat.chat.service;

import com.connectchat.chat.entity.InboxMessage;
import com.connectchat.chat.entity.OutboxMessage;
import java.util.List;
import java.util.UUID;

public interface InboxService {
    void enqueueFromOutbox(OutboxMessage outboxMessage);

    List<InboxMessage> claimNextBatch(int batchSize);

    void markProcessed(UUID id);

    void markFailed(UUID id, String failureReason);
}
