package com.connectchat.storage.service.implementation;

import com.connectchat.storage.common.messaging.MessageStatusEvent;
import com.connectchat.storage.common.messaging.RabbitMessageStatusPublisher;
import com.connectchat.storage.common.messaging.config.MessageStorageMessagingProperties;
import com.connectchat.storage.entity.StorageStatusInboxMessage;
import com.connectchat.storage.service.MessageStorageService;
import com.connectchat.storage.service.StorageStatusInboxService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageStatusInboxProcessor {

    private final StorageStatusInboxService storageStatusInboxService;
    private final MessageStorageService messageStorageService;
    private final RabbitMessageStatusPublisher rabbitMessageStatusPublisher;
    private final MessageStorageMessagingProperties properties;

    @Scheduled(
        fixedDelayString = "${message-storage.messaging.inbox-processing-delay:1000}"
    )
    public void processInboxMessages() {
        List<StorageStatusInboxMessage> messages =
            storageStatusInboxService.claimNextBatch(properties.inboxBatchSize());

        StorageBatchProcessingSupport.processBatch(
            messages,
            message -> {
                messageStorageService.updateStatus(
                    message.getMessageId(),
                    message.getStatusValue()
                );
                rabbitMessageStatusPublisher.publish(
                    new MessageStatusEvent(
                        message.getSourceEventId(),
                        message.getMessageId(),
                        message.getSenderId(),
                        message.getRecipientId(),
                        message.getStatusValue(),
                        message.getActorUserId(),
                        message.getEventOccurredAt()
                    )
                );
            },
            message -> storageStatusInboxService.markProcessed(message.getId()),
            (message, exception) ->
                storageStatusInboxService.markFailed(
                    message.getId(),
                    exception.getMessage()
                ),
            StorageStatusInboxMessage::getId,
            "Failed to update stored message status id={}"
        );
    }
}
