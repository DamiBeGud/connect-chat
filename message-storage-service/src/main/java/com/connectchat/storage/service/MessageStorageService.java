package com.connectchat.storage.service;

import com.connectchat.storage.entity.StorageInboxMessage;
import com.connectchat.storage.entity.StoredMessage;
import com.connectchat.storage.entity.StoredMessageStatus;
import java.util.UUID;

public interface MessageStorageService {
    StoredMessage store(StorageInboxMessage inboxMessage);

    StoredMessage updateStatus(UUID messageId, StoredMessageStatus status);
}
