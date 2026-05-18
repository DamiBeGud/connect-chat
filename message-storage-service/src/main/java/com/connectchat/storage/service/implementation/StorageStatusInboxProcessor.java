package com.connectchat.storage.service.implementation;

import com.connectchat.storage.common.messaging.MessageStatusEvent;
import com.connectchat.storage.common.messaging.RabbitMessageStatusPublisher;
import com.connectchat.storage.common.messaging.config.MessageStorageMessagingProperties;
import com.connectchat.storage.service.StoredMessageStatusUpdateResult;
import com.connectchat.storage.entity.StorageStatusInboxMessage;
import com.connectchat.storage.service.MessageStorageService;
import com.connectchat.storage.service.StorageStatusInboxService;
import java.util.List;
import java.util.Optional;
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

        for (StorageStatusInboxMessage message : messages) {
            try {
                Optional<StoredMessageStatusUpdateResult> updateResult =
                    messageStorageService.updateStatus(
                        message.getMessageId(),
                        message.getStatusValue()
                    );

                if (updateResult.isEmpty()) {
                    // DELIVERED/READ can beat the initial message create flow
                    // through RabbitMQ. Put the inbox row back to PENDING so it
                    // is retried after the Cassandra message row exists.
                    storageStatusInboxService.markPending(message.getId());
                    log.info(
                        "Deferred message status update id={} messageId={} because stored message is not available yet",
                        message.getId(),
                        message.getMessageId()
                    );
                    continue;
                }

                if (updateResult.get().statusChanged()) {
                    // Only publish confirmed status updates when the persisted
                    // state actually advanced. This prevents stale DELIVERED
                    // events from being echoed back after a newer READ.
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
                }

                storageStatusInboxService.markProcessed(message.getId());
            } catch (RuntimeException exception) {
                storageStatusInboxService.markFailed(
                    message.getId(),
                    exception.getMessage()
                );
                log.warn(
                    "Failed to update stored message status id={}",
                    message.getId(),
                    exception
                );
            }
        }
    }
}
