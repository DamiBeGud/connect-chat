package com.connectchat.chat.service;

import com.connectchat.chat.common.messaging.PrivateMessageEvent;
import com.connectchat.chat.entity.InboxMessage;
import java.util.List;
import java.util.UUID;

public interface InboxService {
    void enqueue(PrivateMessageEvent event);

    List<InboxMessage> claimNextBatch(int batchSize);

    void markProcessed(UUID id);

    void markFailed(UUID id, String failureReason);
}
