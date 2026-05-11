package com.connectchat.storage.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.storage.common.messaging.PrivateMessageEvent;
import com.connectchat.storage.entity.MessageInboxStatus;
import com.connectchat.storage.entity.StorageInboxMessage;
import com.connectchat.storage.repository.StorageInboxMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StorageInboxServiceImplTest {

    private final StorageInboxMessageRepository repository =
        org.mockito.Mockito.mock(StorageInboxMessageRepository.class);
    private final StorageInboxServiceImpl service =
        new StorageInboxServiceImpl(repository);

    @Test
    void enqueuesEventOnceWhenSourceMessageIdIsNew() {
        UUID sourceMessageId = UUID.randomUUID();
        PrivateMessageEvent event = new PrivateMessageEvent(
            sourceMessageId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "hello",
            Instant.now()
        );
        when(repository.existsBySourceMessageId(sourceMessageId)).thenReturn(false);

        service.enqueue(event);

        verify(repository).save(org.mockito.ArgumentMatchers.any(StorageInboxMessage.class));
    }

    @Test
    void skipsDuplicateEventWhenSourceMessageIdExists() {
        UUID sourceMessageId = UUID.randomUUID();
        PrivateMessageEvent event = new PrivateMessageEvent(
            sourceMessageId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "hello",
            Instant.now()
        );
        when(repository.existsBySourceMessageId(sourceMessageId)).thenReturn(true);

        service.enqueue(event);

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void claimsPendingRowsAndMarksThemProcessing() {
        StorageInboxMessage message = StorageInboxMessage.builder()
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .content("hello")
            .build();
        when(repository.findBatchForProcessing(10)).thenReturn(List.of(message));

        List<StorageInboxMessage> claimed = service.claimNextBatch(10);

        assertThat(claimed).containsExactly(message);
        assertThat(message.getStatus()).isEqualTo(MessageInboxStatus.PROCESSING);
        assertThat(message.getAttempts()).isEqualTo(1);
    }
}
