package com.connectchat.chat.service.implementation;

import com.connectchat.chat.entity.GroupOutboxMessage;
import com.connectchat.chat.entity.GroupOutboxRecipient;
import com.connectchat.chat.repository.GroupOutboxMessageRepository;
import com.connectchat.chat.repository.GroupOutboxRecipientRepository;
import com.connectchat.chat.service.GroupOutboxService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupOutboxServiceImpl implements GroupOutboxService {

    private final GroupOutboxMessageRepository messageRepository;
    private final GroupOutboxRecipientRepository recipientRepository;

    @Override
    @Transactional
    public void enqueueGroupMessage(
        UUID groupId,
        UUID senderId,
        List<UUID> recipientIds,
        String content
    ) {
        GroupOutboxMessage message = messageRepository.saveAndFlush(
            GroupOutboxMessage.builder()
                .groupId(groupId)
                .senderId(senderId)
                .content(content)
                .build()
        );

        new LinkedHashSet<>(recipientIds)
            .forEach(recipientId ->
                recipientRepository.save(
                    GroupOutboxRecipient.builder()
                        .messageId(message.getId())
                        .recipientId(recipientId)
                        .build()
                )
            );
    }

    @Override
    @Transactional(readOnly = true)
    public GroupOutboxMessage requireMessage(UUID messageId) {
        return messageRepository
            .findById(messageId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Group message not found: " + messageId
                )
            );
    }

    @Override
    @Transactional(readOnly = true)
    public void requireRecipient(UUID messageId, UUID recipientId) {
        if (!recipientRepository.existsByMessageIdAndRecipientId(
            messageId,
            recipientId
        )) {
            throw new AccessDeniedException(
                "Only group message recipients can acknowledge message status"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> recipientIds(UUID messageId) {
        return recipientRepository
            .findByMessageIdOrderByCreatedAtAsc(messageId)
            .stream()
            .map(GroupOutboxRecipient::getRecipientId)
            .toList();
    }

    @Override
    @Transactional
    public List<GroupOutboxMessage> claimNextBatch(int batchSize) {
        List<GroupOutboxMessage> messages =
            messageRepository.findBatchForProcessing(batchSize);
        messages.forEach(GroupOutboxMessage::markProcessing);
        return new ArrayList<>(messages);
    }

    @Override
    @Transactional
    public void markProcessed(UUID id) {
        messageRepository.findById(id).ifPresent(GroupOutboxMessage::markProcessed);
    }

    @Override
    @Transactional
    public void markFailed(UUID id, String failureReason) {
        messageRepository
            .findById(id)
            .ifPresent(message -> message.markFailed(failureReason));
    }
}
