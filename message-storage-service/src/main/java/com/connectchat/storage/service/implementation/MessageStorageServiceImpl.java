package com.connectchat.storage.service.implementation;

import com.connectchat.storage.common.messaging.GroupMessageEvent;
import com.connectchat.storage.entity.GroupStoredMessage;
import com.connectchat.storage.entity.StorageInboxMessage;
import com.connectchat.storage.entity.StoredMessage;
import com.connectchat.storage.entity.StoredMessageStatus;
import com.connectchat.storage.entity.UndeliveredGroupMessage;
import com.connectchat.storage.entity.UndeliveredMessage;
import com.connectchat.storage.repository.GroupStoredMessageRepository;
import com.connectchat.storage.repository.StoredMessageRepository;
import com.connectchat.storage.repository.UndeliveredGroupMessageRepository;
import com.connectchat.storage.repository.UndeliveredMessageRepository;
import com.connectchat.storage.service.MessageStorageService;
import com.connectchat.storage.service.StoredMessageStatusUpdateResult;
import com.connectchat.storage.service.UndeliveredStoredMessage;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageStorageServiceImpl implements MessageStorageService {

    private final StoredMessageRepository storedMessageRepository;
    private final GroupStoredMessageRepository groupStoredMessageRepository;
    private final UndeliveredMessageRepository undeliveredMessageRepository;
    private final UndeliveredGroupMessageRepository undeliveredGroupMessageRepository;

    @Override
    public StoredMessage store(StorageInboxMessage inboxMessage) {
        StoredMessage storedMessage = storedMessageRepository.save(
            StoredMessage.fromInbox(inboxMessage)
        );
        undeliveredMessageRepository.save(
            UndeliveredMessage.fromStoredMessage(storedMessage)
        );
        return storedMessage;
    }

    @Override
    public void storeGroupMessage(GroupMessageEvent event) {
        GroupStoredMessage storedMessage = groupStoredMessageRepository.save(
            GroupStoredMessage.fromEvent(event)
        );
        event
            .recipientIds()
            .stream()
            .distinct()
            .map(recipientId ->
                UndeliveredGroupMessage.fromStoredMessage(
                    storedMessage,
                    recipientId
                )
            )
            .forEach(undeliveredGroupMessageRepository::save);
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
                if (
                    status == StoredMessageStatus.DELIVERED ||
                    status == StoredMessageStatus.READ
                ) {
                    undeliveredMessageRepository.delete(
                        UndeliveredMessage.fromStoredMessage(storedMessage)
                    );
                }
            }

            return new StoredMessageStatusUpdateResult(
                storedMessage,
                statusChanged
            );
        });
    }

    @Override
    public void updateGroupRecipientStatus(
        UUID messageId,
        UUID recipientId,
        StoredMessageStatus status
    ) {
        if (status != StoredMessageStatus.DELIVERED && status != StoredMessageStatus.READ) {
            return;
        }

        groupStoredMessageRepository
            .findById(messageId)
            .map(message ->
                UndeliveredGroupMessage.fromStoredMessage(message, recipientId)
            )
            .ifPresent(undeliveredGroupMessageRepository::delete);
    }

    @Override
    public List<UndeliveredStoredMessage> findUndeliveredMessages(
        UUID recipientId,
        int limit
    ) {
        int normalizedLimit = Math.max(1, limit);
        List<UndeliveredStoredMessage> privateMessages =
            undeliveredMessageRepository
                .findByRecipientId(recipientId, normalizedLimit)
                .stream()
                .map(message ->
                    new UndeliveredStoredMessage(
                        "PRIVATE",
                        message.messageId(),
                        null,
                        message.getSenderId(),
                        message.recipientId(),
                        message.getContent(),
                        message.getStatus(),
                        message.sentAt()
                    )
                )
                .toList();
        List<UndeliveredStoredMessage> groupMessages =
            undeliveredGroupMessageRepository
                .findByRecipientId(recipientId, normalizedLimit)
                .stream()
                .map(message ->
                    new UndeliveredStoredMessage(
                        "GROUP",
                        message.messageId(),
                        message.getGroupId(),
                        message.getSenderId(),
                        message.recipientId(),
                        message.getContent(),
                        message.getStatus(),
                        message.sentAt()
                    )
                )
                .toList();

        return java.util.stream.Stream
            .concat(privateMessages.stream(), groupMessages.stream())
            .sorted(Comparator.comparing(UndeliveredStoredMessage::sentAt))
            .limit(normalizedLimit)
            .toList();
    }
}
