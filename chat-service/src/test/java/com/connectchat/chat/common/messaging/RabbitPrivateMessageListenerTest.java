package com.connectchat.chat.common.messaging;

import static org.mockito.Mockito.verify;

import com.connectchat.chat.service.InboxService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RabbitPrivateMessageListenerTest {

    private final InboxService inboxService = org.mockito.Mockito.mock(
        InboxService.class
    );
    private final RabbitPrivateMessageListener listener =
        new RabbitPrivateMessageListener(inboxService);

    @Test
    void delegatesQueueMessageToInboxService() {
        PrivateMessageEvent event = new PrivateMessageEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "hello",
            Instant.now()
        );

        listener.handlePrivateMessage(event);

        verify(inboxService).enqueue(event);
    }
}
