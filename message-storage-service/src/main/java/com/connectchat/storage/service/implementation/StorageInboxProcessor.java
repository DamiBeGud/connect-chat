package com.connectchat.storage.service.implementation;

import com.connectchat.storage.common.messaging.config.MessageStorageMessagingProperties;
import com.connectchat.storage.common.messaging.MessageStatusEvent;
import com.connectchat.storage.common.messaging.RabbitMessageStatusPublisher;
import com.connectchat.storage.entity.StoredMessageStatus;
import com.connectchat.storage.entity.StorageInboxMessage;
import com.connectchat.storage.service.MessageStorageService;
import com.connectchat.storage.service.StorageInboxService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageInboxProcessor {

    private final StorageInboxService storageInboxService;
    private final MessageStorageService messageStorageService;
    private final RabbitMessageStatusPublisher rabbitMessageStatusPublisher;
    private final MessageStorageMessagingProperties properties;

    @Scheduled(
        fixedDelayString = "${message-storage.messaging.inbox-processing-delay:1000}"
    )
    public void processInboxMessages() {
        List<StorageInboxMessage> messages = storageInboxService.claimNextBatch(
            properties.inboxBatchSize()
        );

        StorageBatchProcessingSupport.processBatch(
            messages,
            message -> {
                messageStorageService.store(message);
                rabbitMessageStatusPublisher.publish(
                    new MessageStatusEvent(
                        // Reuse the inbox row id so a retried SENT confirmation
                        // is deduplicated downstream instead of looking like a
                        // brand-new status event.
                        message.getId(),
                        message.getSourceMessageId() != null
                            ? message.getSourceMessageId()
                            : message.getId(),
                        message.getSenderId(),
                        message.getRecipientId(),
                        StoredMessageStatus.SENT,
                        message.getSenderId(),
                        message.getEventOccurredAt()
                    )
                );
            },
            message -> storageInboxService.markProcessed(message.getId()),
            (message, exception) ->
                storageInboxService.markFailed(
                    message.getId(),
                    exception.getMessage()
                ),
            StorageInboxMessage::getId,
            "Failed to persist inbox message id={}"
        );
    }
}
