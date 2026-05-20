package com.connectchat.chat.service.implementation;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import com.connectchat.chat.api.response.PrivateMessageStatusResponse;
import com.connectchat.chat.common.messaging.PrivateMessageStatus;
import com.connectchat.chat.entity.MessageStatusInboxEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MessageStatusNotificationServiceImplTest {

    private final WebSocketDeliveryFanoutService fanoutService = org.mockito.Mockito.mock(
        WebSocketDeliveryFanoutService.class
    );
    private final MessageStatusNotificationServiceImpl service =
        new MessageStatusNotificationServiceImpl(fanoutService);

    @Test
    void createsPrivateMessageStatusDeliveryTasks() {
        UUID sourceEventId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID actorUserId = recipientId;
        Instant occurredAt = Instant.parse("2026-05-06T10:15:30Z");
        MessageStatusInboxEvent event = MessageStatusInboxEvent.builder()
            .sourceEventId(sourceEventId)
            .messageId(messageId)
            .senderId(senderId)
            .recipientId(recipientId)
            .statusValue(PrivateMessageStatus.DELIVERED)
            .actorUserId(actorUserId)
            .eventOccurredAt(occurredAt)
            .build();

        service.notifyUsers(event);

        verify(fanoutService).createPrivateMessageStatusTasks(
            org.mockito.ArgumentMatchers.eq(sourceEventId),
            argThat((PrivateMessageStatusResponse response) ->
                messageId.equals(response.messageId()) &&
                senderId.equals(response.senderId()) &&
                recipientId.equals(response.recipientId()) &&
                PrivateMessageStatus.DELIVERED.equals(response.status()) &&
                actorUserId.equals(response.actorUserId()) &&
                occurredAt.equals(response.occurredAt())
            )
        );
    }
}
