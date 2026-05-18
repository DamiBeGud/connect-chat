package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.entity.OutboxMessage;
import com.connectchat.chat.repository.OutboxMessageRepository;
import com.connectchat.chat.service.OutboxService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxMessageRepository outboxMessageRepository;

    @Override
    @Transactional
    public void enqueuePrivateMessage(UUID senderId, PrivateMessageRequest request) {
        outboxMessageRepository.save(
            OutboxMessage.builder()
                .senderId(senderId)
                .recipientId(request.recipientId())
                .content(request.content())
                .build()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OutboxMessage requireMessage(UUID messageId) {
        return outboxMessageRepository
            .findById(messageId)
            .orElseThrow(() ->
                new IllegalArgumentException("Message not found: " + messageId)
            );
    }

    @Override
    @Transactional
    public List<OutboxMessage> claimNextBatch(int batchSize) {
        List<OutboxMessage> messages = outboxMessageRepository.findBatchForProcessing(
            batchSize
        );
        messages.forEach(OutboxMessage::markProcessing);
        return new ArrayList<>(messages);
    }

    @Override
    @Transactional
    public void markProcessed(UUID id) {
        outboxMessageRepository.findById(id).ifPresent(OutboxMessage::markProcessed);
    }

    @Override
    @Transactional
    public void markFailed(UUID id, String failureReason) {
        outboxMessageRepository
            .findById(id)
            .ifPresent(message -> message.markFailed(failureReason));
    }
}
