package com.connectchat.chat.service.implementation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.common.messaging.PrivateMessageStatus;
import com.connectchat.chat.entity.OutboxMessage;
import com.connectchat.chat.service.MessageStatusOutboxService;
import com.connectchat.chat.service.OutboxService;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatApplicationServiceImplTest {

    private final OutboxService outboxService = org.mockito.Mockito.mock(
        OutboxService.class
    );
    private final MessageStatusOutboxService messageStatusOutboxService =
        org.mockito.Mockito.mock(MessageStatusOutboxService.class);
    private final ChatApplicationServiceImpl service =
        new ChatApplicationServiceImpl(
            outboxService,
            messageStatusOutboxService
        );

    @Test
    void enqueuesPrivateMessageInOutbox() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        PrivateMessageRequest request = new PrivateMessageRequest(
            recipientId,
            "hello"
        );

        service.handlePrivateMessage(senderId, request);

        verify(outboxService).enqueuePrivateMessage(senderId, request);
    }

    @Test
    void enqueuesDeliveredStatusForRecipientMessage() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        when(outboxService.requireMessage(messageId)).thenReturn(
            OutboxMessage.builder()
                .id(messageId)
                .senderId(senderId)
                .recipientId(recipientId)
                .content("hello")
                .build()
        );

        service.handlePrivateMessageStatus(
            recipientId,
            messageId,
            PrivateMessageStatus.DELIVERED
        );

        verify(messageStatusOutboxService).enqueue(
            messageId,
            senderId,
            recipientId,
            PrivateMessageStatus.DELIVERED,
            recipientId
        );
    }
}
