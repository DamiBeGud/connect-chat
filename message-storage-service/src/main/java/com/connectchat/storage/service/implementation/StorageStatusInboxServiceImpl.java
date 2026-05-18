package com.connectchat.storage.service.implementation;

import com.connectchat.storage.common.messaging.MessageStatusEvent;
import com.connectchat.storage.entity.StorageStatusInboxMessage;
import com.connectchat.storage.repository.StorageStatusInboxMessageRepository;
import com.connectchat.storage.service.StorageStatusInboxService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StorageStatusInboxServiceImpl implements StorageStatusInboxService {

    private final StorageStatusInboxMessageRepository repository;

    @Override
    @Transactional
    public void enqueue(MessageStatusEvent event) {
        if (event.eventId() != null && repository.existsBySourceEventId(event.eventId())) {
            return;
        }

        repository.save(StorageStatusInboxMessage.fromEvent(event));
    }

    @Override
    @Transactional
    public List<StorageStatusInboxMessage> claimNextBatch(int batchSize) {
        List<StorageStatusInboxMessage> messages = repository.findBatchForProcessing(
            batchSize
        );
        messages.forEach(StorageStatusInboxMessage::markProcessing);
        return new ArrayList<>(messages);
    }

    @Override
    @Transactional
    public void markPending(UUID id) {
        repository.findById(id).ifPresent(StorageStatusInboxMessage::markPending);
    }

    @Override
    @Transactional
    public void markProcessed(UUID id) {
        repository.findById(id).ifPresent(StorageStatusInboxMessage::markProcessed);
    }

    @Override
    @Transactional
    public void markFailed(UUID id, String failureReason) {
        repository.findById(id).ifPresent(message -> message.markFailed(failureReason));
    }
}
