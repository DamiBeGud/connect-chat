package com.connectchat.chat.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.chat.entity.MessageProcessingStatus;
import com.connectchat.chat.entity.OutboxMessage;
import com.connectchat.chat.repository.OutboxMessageRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OutboxServiceImplTest {

    private final OutboxMessageRepository repository = org.mockito.Mockito.mock(
        OutboxMessageRepository.class
    );
    private final OutboxServiceImpl service = new OutboxServiceImpl(repository);

    @Test
    void enqueuesPendingOutboxMessage() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        doAnswer(invocation -> invocation.getArgument(0))
            .when(repository)
            .save(any(OutboxMessage.class));

        OutboxMessage saved = service.enqueuePrivateMessage(
            senderId,
            recipientId,
            "hello"
        );

        verify(repository).save(
            any(OutboxMessage.class)
        );
        assertThat(saved.getSenderId()).isEqualTo(senderId);
        assertThat(saved.getRecipientId()).isEqualTo(recipientId);
        assertThat(saved.getContent()).isEqualTo("hello");
    }

    @Test
    void claimsPendingMessagesAndMarksThemProcessing() {
        OutboxMessage message = OutboxMessage.builder()
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .content("hello")
            .build();
        when(repository.findBatchForProcessing(10)).thenReturn(List.of(message));

        List<OutboxMessage> claimed = service.claimNextBatch(10);

        assertThat(claimed).containsExactly(message);
        assertThat(message.getStatus()).isEqualTo(
            MessageProcessingStatus.PROCESSING
        );
        assertThat(message.getAttempts()).isEqualTo(1);
    }
}
