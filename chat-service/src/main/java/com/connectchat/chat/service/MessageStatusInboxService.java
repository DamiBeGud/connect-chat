package com.connectchat.chat.service;

import com.connectchat.chat.common.messaging.MessageStatusEvent;
import com.connectchat.chat.entity.MessageStatusInboxEvent;
import java.util.List;
import java.util.UUID;

public interface MessageStatusInboxService {
    void enqueue(MessageStatusEvent event);

    List<MessageStatusInboxEvent> claimNextBatch(int batchSize);

    void markProcessed(UUID id);

    void markFailed(UUID id, String failureReason);
}
