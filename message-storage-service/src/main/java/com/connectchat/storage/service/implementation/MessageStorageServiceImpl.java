package com.connectchat.storage.service.implementation;

import com.connectchat.storage.entity.StorageInboxMessage;
import com.connectchat.storage.entity.StoredMessage;
import com.connectchat.storage.entity.StoredMessageStatus;
import com.connectchat.storage.repository.StoredMessageRepository;
import com.connectchat.storage.service.MessageStorageService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageStorageServiceImpl implements MessageStorageService {

    private final StoredMessageRepository storedMessageRepository;

    @Override
    public StoredMessage store(StorageInboxMessage inboxMessage) {
        return storedMessageRepository.save(
            StoredMessage.fromInbox(inboxMessage)
        );
    }

    @Override
    public StoredMessage updateStatus(UUID messageId, StoredMessageStatus status) {
        StoredMessage storedMessage = storedMessageRepository
            .findById(messageId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Stored message not found: " + messageId
                )
            );
        storedMessage.updateStatus(status);
        return storedMessageRepository.save(storedMessage);
    }
}
