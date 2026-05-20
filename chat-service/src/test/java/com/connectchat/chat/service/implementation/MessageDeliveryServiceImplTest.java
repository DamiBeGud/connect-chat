package com.connectchat.chat.service.implementation;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import com.connectchat.chat.api.response.PrivateMessageResponse;
import com.connectchat.chat.common.messaging.PrivateMessageCommand;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MessageDeliveryServiceImplTest {

    private final WebSocketDeliveryFanoutService fanoutService = org.mockito.Mockito.mock(
        WebSocketDeliveryFanoutService.class
    );
    private final Clock clock = Clock.fixed(
        Instant.parse("2026-05-06T10:15:30Z"),
        ZoneOffset.UTC
    );
    private final MessageDeliveryServiceImpl service =
        new MessageDeliveryServiceImpl(
            fanoutService,
            clock
        );

    @Test
    void createsPrivateMessageDeliveryTasks() {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        PrivateMessageCommand command = new PrivateMessageCommand(
            messageId,
            senderId,
            recipientId,
            "hello",
            Instant.parse("2026-05-06T10:15:30Z")
        );

        service.deliver(command);

        verify(fanoutService).createPrivateMessageTasks(
            argThat((PrivateMessageResponse message) ->
                messageId.equals(message.messageId()) &&
                senderId.equals(message.senderId()) &&
                recipientId.equals(message.recipientId()) &&
                "hello".equals(message.content()) &&
                Instant.parse("2026-05-06T10:15:30Z").equals(message.sentAt())
            )
        );
    }
}
