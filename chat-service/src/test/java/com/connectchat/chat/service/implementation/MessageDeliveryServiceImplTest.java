package com.connectchat.chat.service.implementation;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.connectchat.chat.common.messaging.PrivateMessageCommand;
import com.connectchat.chat.api.response.PrivateMessageResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

class MessageDeliveryServiceImplTest {

    private final SimpMessagingTemplate messagingTemplate = org.mockito.Mockito.mock(
        SimpMessagingTemplate.class
    );
    private final Clock clock = Clock.fixed(
        Instant.parse("2026-05-06T10:15:30Z"),
        ZoneOffset.UTC
    );
    private final MessageDeliveryServiceImpl service =
        new MessageDeliveryServiceImpl(
            messagingTemplate,
            clock
        );

    @Test
    void deliversPrivateMessageToRecipientAndSender() {
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

        verify(messagingTemplate).convertAndSendToUser(
            eq(recipientId.toString()),
            eq(MessageDeliveryServiceImpl.PRIVATE_MESSAGES_DESTINATION),
            argThat((PrivateMessageResponse message) ->
                messageId.equals(message.messageId()) &&
                senderId.equals(message.senderId()) &&
                recipientId.equals(message.recipientId()) &&
                "hello".equals(message.content()) &&
                Instant.parse("2026-05-06T10:15:30Z").equals(message.sentAt())
            )
        );
        verify(messagingTemplate).convertAndSendToUser(
            eq(senderId.toString()),
            eq(MessageDeliveryServiceImpl.PRIVATE_MESSAGES_DESTINATION),
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
