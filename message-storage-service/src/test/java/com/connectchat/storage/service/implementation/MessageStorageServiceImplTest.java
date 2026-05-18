package com.connectchat.storage.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.storage.entity.StorageInboxMessage;
import com.connectchat.storage.entity.StoredMessage;
import com.connectchat.storage.entity.StoredMessageStatus;
import com.connectchat.storage.repository.StoredMessageRepository;
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

        StoredMessage updated = service.updateStatus(
            messageId,
            StoredMessageStatus.DELIVERED
        );

        assertThat(updated.getStatus()).isEqualTo(
            StoredMessageStatus.DELIVERED.name()
        );
        verify(repository).save(storedMessage);
    }

    @Test
    void throwsWhenUpdatingMissingStoredMessage() {
        UUID messageId = UUID.randomUUID();
        when(repository.findById(messageId)).thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class,
            () -> service.updateStatus(messageId, StoredMessageStatus.READ)
        );
    }
}
