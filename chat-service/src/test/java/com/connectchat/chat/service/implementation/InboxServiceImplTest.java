package com.connectchat.chat.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.chat.entity.InboxMessage;
import com.connectchat.chat.entity.MessageProcessingStatus;
import com.connectchat.chat.entity.OutboxMessage;
import com.connectchat.chat.repository.InboxMessageRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InboxServiceImplTest {

    private final InboxMessageRepository repository = org.mockito.Mockito.mock(
        InboxMessageRepository.class
    );
    private final InboxServiceImpl service = new InboxServiceImpl(repository);

    @Test
    void enqueuesInboxMessageFromOutboxOnce() {
        OutboxMessage outboxMessage = OutboxMessage.builder()
            .id(UUID.randomUUID())
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .content("hello")
            .build();
        when(
            repository.existsBySourceOutboxMessageId(outboxMessage.getId())
        ).thenReturn(false);

        service.enqueueFromOutbox(outboxMessage);

        verify(repository).save(anyInboxMessageFor(outboxMessage));
    }

    @Test
    void skipsDuplicateInboxMessage() {
        OutboxMessage outboxMessage = OutboxMessage.builder()
            .id(UUID.randomUUID())
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .content("hello")
            .build();
        when(
            repository.existsBySourceOutboxMessageId(outboxMessage.getId())
        ).thenReturn(true);

        service.enqueueFromOutbox(outboxMessage);

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void claimsPendingInboxMessagesAndMarksThemProcessing() {
        InboxMessage message = InboxMessage.builder()
            .sourceOutboxMessageId(UUID.randomUUID())
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .content("hello")
            .build();
        when(repository.findBatchForProcessing(10)).thenReturn(List.of(message));

        List<InboxMessage> claimed = service.claimNextBatch(10);

        assertThat(claimed).containsExactly(message);
        assertThat(message.getStatus()).isEqualTo(
            MessageProcessingStatus.PROCESSING
        );
        assertThat(message.getAttempts()).isEqualTo(1);
    }

    private InboxMessage anyInboxMessageFor(OutboxMessage outboxMessage) {
        return org.mockito.ArgumentMatchers.argThat(message ->
            outboxMessage.getId().equals(message.getSourceOutboxMessageId()) &&
            outboxMessage.getSenderId().equals(message.getSenderId()) &&
            outboxMessage.getRecipientId().equals(message.getRecipientId()) &&
            outboxMessage.getContent().equals(message.getContent())
        );
    }
}
