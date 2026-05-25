package com.connectchat.storage.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.storage.entity.StorageInboxMessage;
import com.connectchat.storage.entity.StoredMessage;
import com.connectchat.storage.entity.StoredMessageStatus;
import com.connectchat.storage.entity.UndeliveredMessage;
import com.connectchat.storage.repository.GroupStoredMessageRepository;
import com.connectchat.storage.repository.StoredMessageRepository;
import com.connectchat.storage.repository.UndeliveredGroupMessageRepository;
import com.connectchat.storage.repository.UndeliveredMessageRepository;
import com.connectchat.storage.service.StoredMessageStatusUpdateResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MessageStorageServiceImplTest {

    private final StoredMessageRepository repository = org.mockito.Mockito.mock(
        StoredMessageRepository.class
    );
    private final GroupStoredMessageRepository groupStoredMessageRepository =
        org.mockito.Mockito.mock(GroupStoredMessageRepository.class);
    private final UndeliveredMessageRepository undeliveredMessageRepository =
        org.mockito.Mockito.mock(UndeliveredMessageRepository.class);
    private final UndeliveredGroupMessageRepository undeliveredGroupMessageRepository =
        org.mockito.Mockito.mock(UndeliveredGroupMessageRepository.class);
    private final MessageStorageServiceImpl service =
        new MessageStorageServiceImpl(
            repository,
            groupStoredMessageRepository,
            undeliveredMessageRepository,
            undeliveredGroupMessageRepository
        );

    @Test
    void storesMessageFromInboxRow() {
        UUID messageId = UUID.randomUUID();
        StorageInboxMessage inboxMessage = StorageInboxMessage.builder()
            .id(messageId)
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .content("hello")
            .eventOccurredAt(Instant.parse("2026-05-06T10:15:30Z"))
            .build();
        when(repository.save(org.mockito.ArgumentMatchers.any(StoredMessage.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        service.store(inboxMessage);

        verify(repository).save(org.mockito.ArgumentMatchers.any(StoredMessage.class));
        verify(undeliveredMessageRepository)
            .save(org.mockito.ArgumentMatchers.any(UndeliveredMessage.class));
    }

    @Test
    void updatesStoredMessageStatus() {
        UUID messageId = UUID.randomUUID();
        StoredMessage storedMessage = StoredMessage.builder()
            .messageId(messageId)
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .sentAt(Instant.parse("2026-05-06T10:15:30Z"))
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
        verify(undeliveredMessageRepository)
            .delete(org.mockito.ArgumentMatchers.any(UndeliveredMessage.class));
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
        verify(undeliveredMessageRepository, never())
            .delete(org.mockito.ArgumentMatchers.any(UndeliveredMessage.class));
    }

    @Test
    void findsUndeliveredMessagesWithMinimumLimit() {
        UUID recipientId = UUID.randomUUID();
        when(undeliveredMessageRepository.findByRecipientId(recipientId, 1))
            .thenReturn(List.of());
        when(undeliveredGroupMessageRepository.findByRecipientId(recipientId, 1))
            .thenReturn(List.of());

        assertThat(service.findUndeliveredMessages(recipientId, 0)).isEmpty();

        verify(undeliveredMessageRepository).findByRecipientId(recipientId, 1);
        verify(undeliveredGroupMessageRepository).findByRecipientId(recipientId, 1);
    }
}
