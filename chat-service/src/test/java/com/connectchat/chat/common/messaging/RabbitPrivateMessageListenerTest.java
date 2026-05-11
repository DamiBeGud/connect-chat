package com.connectchat.chat.common.messaging;

import static org.mockito.Mockito.verify;

import com.connectchat.chat.service.MessageDeliveryService;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RabbitPrivateMessageListenerTest {

    private final MessageDeliveryService messageDeliveryService =
        org.mockito.Mockito.mock(MessageDeliveryService.class);
    private final RabbitPrivateMessageListener listener =
        new RabbitPrivateMessageListener(messageDeliveryService);

    @Test
    void delegatesQueueMessageToDeliveryService() {
        PrivateMessageCommand command = new PrivateMessageCommand(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "hello"
        );

        listener.handlePrivateMessage(command);

        verify(messageDeliveryService).deliver(command);
    }
}
