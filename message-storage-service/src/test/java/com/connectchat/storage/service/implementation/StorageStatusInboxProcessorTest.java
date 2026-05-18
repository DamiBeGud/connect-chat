package com.connectchat.storage.service.implementation;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.storage.common.messaging.RabbitMessageStatusPublisher;
import com.connectchat.storage.common.messaging.config.MessageStorageMessagingProperties;
import com.connectchat.storage.entity.StorageStatusInboxMessage;
import com.connectchat.storage.entity.StoredMessage;
import com.connectchat.storage.entity.StoredMessageStatus;
import com.connectchat.storage.service.MessageStorageService;
import com.connectchat.storage.service.StorageStatusInboxService;
import com.connectchat.storage.service.StoredMessageStatusUpdateResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StorageStatusInboxProcessorTest {

    private final StorageStatusInboxService storageStatusInboxService =
        org.mockito.Mockito.mock(StorageStatusInboxService.class);
    private final MessageStorageService messageStorageService =
        org.mockito.Mockito.mock(MessageStorageService.class);
    private final RabbitMessageStatusPublisher rabbitMessageStatusPublisher =
        org.mockito.Mockito.mock(RabbitMessageStatusPublisher.class);
    private final MessageStorageMessagingProperties properties =
        new MessageStorageMessagingProperties(
            "message-storage.private-message.queue",
            "chat.private-message.exchange",
            "chat.private-message",
            "message-storage.message-status.request.queue",
            "chat.message-status.request.exchange",
            "chat.message-status.request",
            "chat.message-status.confirmed.exchange",
            "chat.message-status.confirmed",
            25,
            1000L
        );
    private final StorageStatusInboxProcessor processor =
        new StorageStatusInboxProcessor(
            storageStatusInboxService,
            messageStorageService,
            rabbitMessageStatusPublisher,
            properties
        );

    @Test
    void marksStatusEventPendingWhenStoredMessageIsNotAvailableYet() {
        StorageStatusInboxMessage message = StorageStatusInboxMessage.builder()
            .id(UUID.randomUUID())
            .sourceEventId(UUID.randomUUID())
            .messageId(UUID.randomUUID())
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .statusValue(StoredMessageStatus.DELIVERED)
            .actorUserId(UUID.randomUUID())
            .eventOccurredAt(Instant.now())
            .build();
        when(storageStatusInboxService.claimNextBatch(25)).thenReturn(List.of(message));
        when(
            messageStorageService.updateStatus(
                message.getMessageId(),
                message.getStatusValue()
            )
        ).thenReturn(Optional.empty());

        processor.processInboxMessages();

        verify(storageStatusInboxService).markPending(message.getId());
        verify(storageStatusInboxService, never()).markProcessed(message.getId());
        verify(rabbitMessageStatusPublisher, never()).publish(
            org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void doesNotRepublishStaleStatusUpdates() {
        StorageStatusInboxMessage message = StorageStatusInboxMessage.builder()
            .id(UUID.randomUUID())
            .sourceEventId(UUID.randomUUID())
            .messageId(UUID.randomUUID())
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .statusValue(StoredMessageStatus.DELIVERED)
            .actorUserId(UUID.randomUUID())
            .eventOccurredAt(Instant.now())
            .build();
        when(storageStatusInboxService.claimNextBatch(25)).thenReturn(List.of(message));
        when(
            messageStorageService.updateStatus(
                message.getMessageId(),
                message.getStatusValue()
            )
        ).thenReturn(
            Optional.of(
                new StoredMessageStatusUpdateResult(
                    StoredMessage.builder()
                        .messageId(message.getMessageId())
                        .status(StoredMessageStatus.READ.name())
                        .build(),
                    false
                )
            )
        );

        processor.processInboxMessages();

        verify(storageStatusInboxService).markProcessed(message.getId());
        verify(rabbitMessageStatusPublisher, never()).publish(
            org.mockito.ArgumentMatchers.any()
        );
    }
}
