package com.connectchat.chat.service.implementation;

import com.connectchat.chat.common.messaging.PrivateMessageStatus;
import com.connectchat.chat.entity.MessageStatusOutboxEvent;
import com.connectchat.chat.repository.MessageStatusOutboxEventRepository;
import com.connectchat.chat.service.MessageStatusOutboxService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageStatusOutboxServiceImpl implements MessageStatusOutboxService {

    private final MessageStatusOutboxEventRepository repository;

    @Override
    @Transactional
    public void enqueue(
        UUID messageId,
        UUID senderId,
        UUID recipientId,
        PrivateMessageStatus status,
        UUID actorUserId
    ) {
        repository.save(
            MessageStatusOutboxEvent.builder()
                .messageId(messageId)
                .senderId(senderId)
                .recipientId(recipientId)
                .statusValue(status)
                .actorUserId(actorUserId)
                .eventOccurredAt(Instant.now())
                .build()
        );
    }

    @Override
    @Transactional
    public List<MessageStatusOutboxEvent> claimNextBatch(int batchSize) {
        List<MessageStatusOutboxEvent> events = repository.findBatchForProcessing(
            batchSize
        );
        events.forEach(MessageStatusOutboxEvent::markProcessing);
        return new ArrayList<>(events);
    }

    @Override
    @Transactional
    public void markProcessed(UUID id) {
        repository.findById(id).ifPresent(MessageStatusOutboxEvent::markProcessed);
    }

    @Override
    @Transactional
    public void markFailed(UUID id, String failureReason) {
        repository
            .findById(id)
            .ifPresent(event -> event.markFailed(failureReason));
    }
}
