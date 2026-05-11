package com.connectchat.chat.service.implementation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.chat.common.messaging.PrivateMessageCommand;
import com.connectchat.chat.entity.InboxMessage;
import com.connectchat.chat.entity.OutboxMessage;
import com.connectchat.chat.service.InboxService;
import com.connectchat.chat.service.MessageDeliveryService;
import com.connectchat.chat.service.OutboxService;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatMessagePipelineServiceTest {

    private final OutboxService outboxService = org.mockito.Mockito.mock(
        OutboxService.class
    );
    private final InboxService inboxService = org.mockito.Mockito.mock(
        InboxService.class
    );
    private final MessageDeliveryService messageDeliveryService =
        org.mockito.Mockito.mock(MessageDeliveryService.class);
    private final ChatMessagePipelineService service =
        new ChatMessagePipelineService(
            outboxService,
            inboxService,
            messageDeliveryService
        );

    @Test
    void movesClaimedOutboxMessagesToInbox() throws Exception {
        OutboxMessage outboxMessage = OutboxMessage.builder()
            .id(UUID.randomUUID())
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .content("hello")
            .build();
        when(outboxService.claimNextBatch(5)).thenReturn(List.of(outboxMessage));
        setField("outboxBatchSize", 5);

        service.processOutboxMessages();

        verify(inboxService).enqueueFromOutbox(outboxMessage);
        verify(outboxService).markProcessed(outboxMessage.getId());
    }

    @Test
    void deliversClaimedInboxMessagesToWebSocketUsers() throws Exception {
        InboxMessage inboxMessage = InboxMessage.builder()
            .id(UUID.randomUUID())
            .sourceOutboxMessageId(UUID.randomUUID())
            .senderId(UUID.randomUUID())
            .recipientId(UUID.randomUUID())
            .content("hello")
            .build();
        when(inboxService.claimNextBatch(3)).thenReturn(List.of(inboxMessage));
        setField("inboxBatchSize", 3);

        service.processInboxMessages();

        verify(messageDeliveryService).deliver(
            new PrivateMessageCommand(
                inboxMessage.getSenderId(),
                inboxMessage.getRecipientId(),
                inboxMessage.getContent()
            )
        );
        verify(inboxService).markProcessed(inboxMessage.getId());
    }

    private void setField(String name, int value) throws Exception {
        Field field = ChatMessagePipelineService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(service, value);
    }
}
