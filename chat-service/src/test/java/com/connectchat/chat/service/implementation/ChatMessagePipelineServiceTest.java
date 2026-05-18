package com.connectchat.chat.service.implementation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.chat.common.messaging.PrivateMessageCommand;
import com.connectchat.chat.common.messaging.RabbitPrivateMessagePublisher;
import com.connectchat.chat.common.messaging.config.ChatMessagingProperties;
import com.connectchat.chat.entity.InboxMessage;
import com.connectchat.chat.entity.OutboxMessage;
import com.connectchat.chat.service.InboxService;
import com.connectchat.chat.service.MessageDeliveryService;
import com.connectchat.chat.service.OutboxService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatMessagePipelineServiceTest {

    private final OutboxService outboxService = org.mockito.Mockito.mock(
        OutboxService.class
    );
    private final RabbitPrivateMessagePublisher rabbitPrivateMessagePublisher =
        org.mockito.Mockito.mock(RabbitPrivateMessagePublisher.class);
    private final InboxService inboxService = org.mockito.Mockito.mock(
        InboxService.class
    );
    private final MessageDeliveryService messageDeliveryService =
        org.mockito.Mockito.mock(MessageDeliveryService.class);
    private final ChatMessagingProperties properties = new ChatMessagingProperties(
        "chat.private-message.queue",
        "chat.private-message.exchange",
        "chat.private-message",
        5,
        3,
        1000L,
        1000L
    );
    private final ChatMessagePipelineService service =
        new ChatMessagePipelineService(
            outboxService,
            rabbitPrivateMessagePublisher,
            inboxService,
            messageDeliveryService,
            properties
        );

    @Test
    void publishesClaimedOutboxMessagesToRabbit() {
        OutboxMessage outboxMessage = OutboxMessage.builder()
            .id(UUID.randomUUID())
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .content("hello")
            .build();
        when(outboxService.claimNextBatch(5)).thenReturn(List.of(outboxMessage));

        service.processOutboxMessages();

        verify(rabbitPrivateMessagePublisher).publish(outboxMessage.toEvent());
        verify(outboxService).markProcessed(outboxMessage.getId());
    }

    @Test
    void deliversClaimedInboxMessagesToWebSocketUsers() {
        Instant occurredAt = Instant.now();
        InboxMessage inboxMessage = InboxMessage.builder()
            .id(UUID.randomUUID())
            .sourceMessageId(UUID.randomUUID())
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .content("hello")
            .occurredAt(occurredAt)
            .build();
        when(inboxService.claimNextBatch(3)).thenReturn(List.of(inboxMessage));

        service.processInboxMessages();

        verify(messageDeliveryService).deliver(
            new PrivateMessageCommand(
                inboxMessage.getSourceMessageId(),
                inboxMessage.getSenderId(),
                inboxMessage.getRecipientId(),
                inboxMessage.getContent(),
                occurredAt
            )
        );
        verify(inboxService).markProcessed(inboxMessage.getId());
    }
}
