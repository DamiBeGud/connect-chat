package com.connectchat.chat.service.implementation;

import com.connectchat.chat.common.messaging.PrivateMessageEvent;
import com.connectchat.chat.entity.InboxMessage;
import com.connectchat.chat.repository.InboxMessageRepository;
import com.connectchat.chat.service.InboxService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InboxServiceImpl implements InboxService {

    private final InboxMessageRepository inboxMessageRepository;

    @Override
    @Transactional
    public void enqueue(PrivateMessageEvent event) {
        if (
            event.messageId() != null &&
            inboxMessageRepository.existsBySourceMessageId(event.messageId())
        ) {
            return;
        }

        inboxMessageRepository.save(InboxMessage.fromEvent(event));
    }

    @Override
    @Transactional
    public List<InboxMessage> claimNextBatch(int batchSize) {
        List<InboxMessage> messages = inboxMessageRepository.findBatchForProcessing(
            batchSize
        );
        messages.forEach(InboxMessage::markProcessing);
        return new ArrayList<>(messages);
    }

    @Override
    @Transactional
    public void markProcessed(UUID id) {
        inboxMessageRepository.findById(id).ifPresent(InboxMessage::markProcessed);
    }

    @Override
    @Transactional
    public void markFailed(UUID id, String failureReason) {
        inboxMessageRepository
            .findById(id)
            .ifPresent(message -> message.markFailed(failureReason));
    }
}
