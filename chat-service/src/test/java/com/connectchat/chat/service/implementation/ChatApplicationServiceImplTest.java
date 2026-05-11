package com.connectchat.chat.service.implementation;

import static org.mockito.Mockito.verify;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.service.OutboxService;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatApplicationServiceImplTest {

    private final OutboxService outboxService = org.mockito.Mockito.mock(
        OutboxService.class
    );
    private final ChatApplicationServiceImpl service =
        new ChatApplicationServiceImpl(outboxService);

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
}
