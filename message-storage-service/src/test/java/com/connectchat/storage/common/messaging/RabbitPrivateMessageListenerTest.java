package com.connectchat.storage.common.messaging;

import static org.mockito.Mockito.verify;

import com.connectchat.storage.service.StorageInboxService;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RabbitPrivateMessageListenerTest {

    private final StorageInboxService storageInboxService = org.mockito.Mockito.mock(
        StorageInboxService.class
    );
    private final RabbitPrivateMessageListener listener =
        new RabbitPrivateMessageListener(storageInboxService);

    @Test
    void delegatesQueueMessageToInboxService() {
        PrivateMessageEvent event = new PrivateMessageEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "hello",
            java.time.Instant.now()
        );

        listener.handlePrivateMessage(event);

        verify(storageInboxService).enqueue(event);
    }
}
