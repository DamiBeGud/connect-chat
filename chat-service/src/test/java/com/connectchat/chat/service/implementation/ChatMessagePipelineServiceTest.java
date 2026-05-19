package com.connectchat.chat.service.implementation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;

import com.connectchat.chat.common.messaging.PrivateMessageCommand;
import com.connectchat.chat.common.messaging.RabbitMessageStatusRequestPublisher;
import com.connectchat.chat.common.messaging.RabbitPrivateMessagePublisher;
import com.connectchat.chat.common.messaging.config.ChatMessagingProperties;
import com.connectchat.chat.entity.InboxMessage;
import com.connectchat.chat.entity.MessageStatusInboxEvent;
import com.connectchat.chat.entity.MessageStatusOutboxEvent;
import com.connectchat.chat.entity.OutboxMessage;
import com.connectchat.chat.service.InboxService;
import com.connectchat.chat.service.MessageDeliveryService;
import com.connectchat.chat.service.MessageStatusInboxService;
import com.connectchat.chat.service.MessageStatusNotificationService;
import com.connectchat.chat.service.MessageStatusOutboxService;
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
    private final MessageStatusOutboxService messageStatusOutboxService =
        org.mockito.Mockito.mock(MessageStatusOutboxService.class);
    private final RabbitMessageStatusRequestPublisher rabbitMessageStatusRequestPublisher =
        org.mockito.Mockito.mock(RabbitMessageStatusRequestPublisher.class);
    private final MessageStatusInboxService messageStatusInboxService =
        org.mockito.Mockito.mock(MessageStatusInboxService.class);
    private final MessageStatusNotificationService messageStatusNotificationService =
        org.mockito.Mockito.mock(MessageStatusNotificationService.class);
    private final ChatMessagingProperties properties = new ChatMessagingProperties(
        "chat.private-message.queue",
        "chat.private-message.exchange",
        "chat.private-message",
        "chat.message-status.request.queue",
        "chat.message-status.request.exchange",
        "chat.message-status.request",
        "chat.message-status.confirmed.queue",
        "chat.message-status.confirmed.exchange",
        "chat.message-status.confirmed",
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
            messageStatusOutboxService,
            rabbitMessageStatusRequestPublisher,
            messageStatusInboxService,
            messageStatusNotificationService,
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

        verify(rabbitPrivateMessagePublisher).publish(
            argThat(event ->
                event.messageId().equals(outboxMessage.getId()) &&
                event.senderId().equals(outboxMessage.getSenderId()) &&
                event.recipientId().equals(outboxMessage.getRecipientId()) &&
                event.content().equals(outboxMessage.getContent()) &&
                event.occurredAt() != null
            )
        );
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

    @Test
    void publishesClaimedStatusOutboxEventsToRabbit() {
        MessageStatusOutboxEvent event = MessageStatusOutboxEvent.builder()
            .id(UUID.randomUUID())
            .messageId(UUID.randomUUID())
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .statusValue(com.connectchat.chat.common.messaging.PrivateMessageStatus.DELIVERED)
            .actorUserId(UUID.randomUUID())
            .eventOccurredAt(Instant.now())
            .build();
        when(messageStatusOutboxService.claimNextBatch(5)).thenReturn(List.of(event));

        service.processStatusOutboxEvents();

        verify(rabbitMessageStatusRequestPublisher).publish(event.toEvent());
        verify(messageStatusOutboxService).markProcessed(event.getId());
    }

    @Test
    void notifiesUsersAboutClaimedStatusInboxEvents() {
        MessageStatusInboxEvent event = MessageStatusInboxEvent.builder()
            .id(UUID.randomUUID())
            .sourceEventId(UUID.randomUUID())
            .messageId(UUID.randomUUID())
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .statusValue(com.connectchat.chat.common.messaging.PrivateMessageStatus.SENT)
            .actorUserId(UUID.randomUUID())
            .eventOccurredAt(Instant.now())
            .build();
        when(messageStatusInboxService.claimNextBatch(3)).thenReturn(List.of(event));

        service.processStatusInboxEvents();

        verify(messageStatusNotificationService).notifyUsers(event);
        verify(messageStatusInboxService).markProcessed(event.getId());
    }
}
