package com.connectchat.storage.service.implementation;

import com.connectchat.storage.common.messaging.config.MessageStorageMessagingProperties;
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
    private final MessageStorageMessagingProperties properties;

    @Scheduled(
        fixedDelayString = "${message-storage.messaging.inbox-processing-delay:1000}"
    )
    public void processInboxMessages() {
        List<StorageInboxMessage> messages = storageInboxService.claimNextBatch(
            properties.inboxBatchSize()
        );

        for (StorageInboxMessage message : messages) {
            try {
                messageStorageService.store(message);
                storageInboxService.markProcessed(message.getId());
            } catch (RuntimeException exception) {
                storageInboxService.markFailed(
                    message.getId(),
                    exception.getMessage()
                );
                log.warn(
                    "Failed to persist inbox message id={}",
                    message.getId(),
                    exception
                );
            }
        }
    }
}
