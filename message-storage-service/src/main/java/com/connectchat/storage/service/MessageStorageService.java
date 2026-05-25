package com.connectchat.storage.service;

import com.connectchat.storage.common.messaging.GroupMessageEvent;
import com.connectchat.storage.entity.StorageInboxMessage;
import com.connectchat.storage.entity.StoredMessage;
import com.connectchat.storage.entity.StoredMessageStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageStorageService {
    StoredMessage store(StorageInboxMessage inboxMessage);

    void storeGroupMessage(GroupMessageEvent event);

    Optional<StoredMessageStatusUpdateResult> updateStatus(
        UUID messageId,
        StoredMessageStatus status
    );

    void updateGroupRecipientStatus(
        UUID messageId,
        UUID recipientId,
        StoredMessageStatus status
    );

    List<UndeliveredStoredMessage> findUndeliveredMessages(
        UUID recipientId,
        int limit
    );
}
