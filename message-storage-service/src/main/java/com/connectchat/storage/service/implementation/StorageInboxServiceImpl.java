package com.connectchat.storage.service.implementation;

import com.connectchat.storage.common.messaging.PrivateMessageEvent;
import com.connectchat.storage.entity.StorageInboxMessage;
import com.connectchat.storage.repository.StorageInboxMessageRepository;
import com.connectchat.storage.service.StorageInboxService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StorageInboxServiceImpl implements StorageInboxService {

    private final StorageInboxMessageRepository storageInboxMessageRepository;

    @Override
    @Transactional
    public void enqueue(PrivateMessageEvent event) {
        if (
            event.messageId() != null &&
            storageInboxMessageRepository.existsBySourceMessageId(event.messageId())
        ) {
            return;
        }

        storageInboxMessageRepository.save(StorageInboxMessage.fromEvent(event));
    }

    @Override
    @Transactional
    public List<StorageInboxMessage> claimNextBatch(int batchSize) {
        List<StorageInboxMessage> messages =
            storageInboxMessageRepository.findBatchForProcessing(batchSize);
        messages.forEach(StorageInboxMessage::markProcessing);
        return new ArrayList<>(messages);
    }

    @Override
    @Transactional
    public void markProcessed(UUID id) {
        storageInboxMessageRepository
            .findById(id)
            .ifPresent(StorageInboxMessage::markProcessed);
    }

    @Override
    @Transactional
    public void markFailed(UUID id, String failureReason) {
        storageInboxMessageRepository
            .findById(id)
            .ifPresent(message -> message.markFailed(failureReason));
    }
}
