package com.connectchat.storage.service.implementation;

import com.connectchat.storage.entity.StorageInboxMessage;
import com.connectchat.storage.entity.StoredMessage;
import com.connectchat.storage.entity.StoredMessageStatus;
import com.connectchat.storage.repository.StoredMessageRepository;
import com.connectchat.storage.service.MessageStorageService;
import com.connectchat.storage.service.StoredMessageStatusUpdateResult;
import java.util.Optional;
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
    public Optional<StoredMessageStatusUpdateResult> updateStatus(
        UUID messageId,
        StoredMessageStatus status
    ) {
        // Status events can arrive before the initial message create event is
        // stored. Returning empty lets the inbox processor requeue the event
        // instead of treating that race as a terminal failure.
        return storedMessageRepository.findById(messageId).map(storedMessage -> {
            boolean statusChanged = storedMessage.updateStatus(status);
            if (statusChanged) {
                storedMessage = storedMessageRepository.save(storedMessage);
            }

            return new StoredMessageStatusUpdateResult(
                storedMessage,
                statusChanged
            );
        });
    }
}
