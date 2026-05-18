package com.connectchat.storage.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.storage.entity.StorageInboxMessage;
import com.connectchat.storage.entity.StoredMessage;
import com.connectchat.storage.entity.StoredMessageStatus;
import com.connectchat.storage.repository.StoredMessageRepository;
import com.connectchat.storage.service.StoredMessageStatusUpdateResult;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MessageStorageServiceImplTest {

    private final StoredMessageRepository repository = org.mockito.Mockito.mock(
        StoredMessageRepository.class
    );
    private final MessageStorageServiceImpl service =
        new MessageStorageServiceImpl(repository);

    @Test
    void storesMessageFromInboxRow() {
        StorageInboxMessage inboxMessage = StorageInboxMessage.builder()
            .id(UUID.randomUUID())
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .content("hello")
            .build();

        service.store(inboxMessage);

        verify(repository).save(org.mockito.ArgumentMatchers.any(StoredMessage.class));
    }

    @Test
    void updatesStoredMessageStatus() {
        UUID messageId = UUID.randomUUID();
        StoredMessage storedMessage = StoredMessage.builder()
            .messageId(messageId)
            .status(StoredMessageStatus.SENT.name())
            .build();
        when(repository.findById(messageId)).thenReturn(Optional.of(storedMessage));
        when(repository.save(storedMessage)).thenReturn(storedMessage);

        StoredMessageStatusUpdateResult updateResult = service
            .updateStatus(
            messageId,
            StoredMessageStatus.DELIVERED
        )
            .orElseThrow();

        assertThat(updateResult.statusChanged()).isTrue();
        assertThat(updateResult.storedMessage().getStatus()).isEqualTo(
            StoredMessageStatus.DELIVERED.name()
        );
        verify(repository).save(storedMessage);
    }

    @Test
    void returnsEmptyWhenUpdatingMissingStoredMessage() {
        UUID messageId = UUID.randomUUID();
        when(repository.findById(messageId)).thenReturn(Optional.empty());

        assertThat(service.updateStatus(messageId, StoredMessageStatus.READ)).isEmpty();
    }

    @Test
    void doesNotSaveOrSignalChangeForStaleStatusUpdate() {
        UUID messageId = UUID.randomUUID();
        StoredMessage storedMessage = StoredMessage.builder()
            .messageId(messageId)
            .status(StoredMessageStatus.READ.name())
            .build();
        when(repository.findById(messageId)).thenReturn(Optional.of(storedMessage));

        StoredMessageStatusUpdateResult updateResult = service
            .updateStatus(messageId, StoredMessageStatus.DELIVERED)
            .orElseThrow();

        assertThat(updateResult.statusChanged()).isFalse();
        assertThat(updateResult.storedMessage().getStatus()).isEqualTo(
            StoredMessageStatus.READ.name()
        );
        verify(repository, never()).save(storedMessage);
    }
}
