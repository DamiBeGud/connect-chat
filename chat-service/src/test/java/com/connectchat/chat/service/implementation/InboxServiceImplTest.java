package com.connectchat.chat.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.chat.common.messaging.PrivateMessageEvent;
import com.connectchat.chat.entity.InboxMessage;
import com.connectchat.chat.entity.MessageProcessingStatus;
import com.connectchat.chat.repository.InboxMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InboxServiceImplTest {

    private final InboxMessageRepository repository = org.mockito.Mockito.mock(
        InboxMessageRepository.class
    );
    private final InboxServiceImpl service = new InboxServiceImpl(repository);

    @Test
    void enqueuesInboxMessageFromEventOnce() {
        PrivateMessageEvent event = new PrivateMessageEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "hello",
            Instant.now()
        );
        when(repository.existsBySourceMessageId(event.messageId())).thenReturn(false);

        service.enqueue(event);

        verify(repository).save(anyInboxMessageFor(event));
    }

    @Test
    void skipsDuplicateInboxMessage() {
        PrivateMessageEvent event = new PrivateMessageEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "hello",
            Instant.now()
        );
        when(repository.existsBySourceMessageId(event.messageId())).thenReturn(true);

        service.enqueue(event);

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void claimsPendingInboxMessagesAndMarksThemProcessing() {
        InboxMessage message = InboxMessage.builder()
            .sourceMessageId(UUID.randomUUID())
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

    private InboxMessage anyInboxMessageFor(PrivateMessageEvent event) {
        return org.mockito.ArgumentMatchers.argThat(message ->
            event.messageId().equals(message.getSourceMessageId()) &&
            event.senderId().equals(message.getSenderId()) &&
            event.recipientId().equals(message.getRecipientId()) &&
            event.content().equals(message.getContent()) &&
            event.occurredAt().equals(message.getOccurredAt())
        );
    }
}
