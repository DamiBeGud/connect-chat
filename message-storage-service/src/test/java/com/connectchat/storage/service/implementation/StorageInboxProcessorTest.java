package com.connectchat.storage.service.implementation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.storage.common.messaging.config.MessageStorageMessagingProperties;
import com.connectchat.storage.entity.StorageInboxMessage;
import com.connectchat.storage.service.MessageStorageService;
import com.connectchat.storage.service.StorageInboxService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StorageInboxProcessorTest {

    private final StorageInboxService storageInboxService =
        org.mockito.Mockito.mock(StorageInboxService.class);
    private final MessageStorageService messageStorageService =
        org.mockito.Mockito.mock(MessageStorageService.class);
    private final MessageStorageMessagingProperties properties =
        new MessageStorageMessagingProperties(
            "message-storage.private-message.queue",
            "chat.private-message.exchange",
            "chat.private-message",
            25,
            1000L
    );
    private final StorageInboxProcessor processor = new StorageInboxProcessor(
        storageInboxService,
        messageStorageService,
        properties
    );

    @Test
    void storesClaimedInboxMessagesAndMarksThemProcessed() {
        StorageInboxMessage message = StorageInboxMessage.builder()
            .id(UUID.randomUUID())
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .content("hello")
            .eventOccurredAt(Instant.now())
            .build();
        when(storageInboxService.claimNextBatch(25)).thenReturn(List.of(message));

        processor.processInboxMessages();

        verify(messageStorageService).store(message);
        verify(storageInboxService).markProcessed(message.getId());
    }
}
