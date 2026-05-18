package com.connectchat.chat.service.implementation;

import com.connectchat.chat.common.messaging.MessageStatusEvent;
import com.connectchat.chat.entity.MessageStatusInboxEvent;
import com.connectchat.chat.repository.MessageStatusInboxEventRepository;
import com.connectchat.chat.service.MessageStatusInboxService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageStatusInboxServiceImpl implements MessageStatusInboxService {

    private final MessageStatusInboxEventRepository repository;

    @Override
    @Transactional
    public void enqueue(MessageStatusEvent event) {
        if (event.eventId() != null && repository.existsBySourceEventId(event.eventId())) {
            return;
        }

        repository.save(MessageStatusInboxEvent.fromEvent(event));
    }

    @Override
    @Transactional
    public List<MessageStatusInboxEvent> claimNextBatch(int batchSize) {
        List<MessageStatusInboxEvent> events = repository.findBatchForProcessing(
            batchSize
        );
        events.forEach(MessageStatusInboxEvent::markProcessing);
        return new ArrayList<>(events);
    }

    @Override
    @Transactional
    public void markProcessed(UUID id) {
        repository.findById(id).ifPresent(MessageStatusInboxEvent::markProcessed);
    }

    @Override
    @Transactional
    public void markFailed(UUID id, String failureReason) {
        repository
            .findById(id)
            .ifPresent(event -> event.markFailed(failureReason));
    }
}
