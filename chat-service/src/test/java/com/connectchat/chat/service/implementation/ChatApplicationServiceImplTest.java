package com.connectchat.chat.service.implementation;

import static org.mockito.Mockito.verify;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.common.messaging.PrivateMessagePublisher;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatApplicationServiceImplTest {

    private final PrivateMessagePublisher privateMessagePublisher =
        org.mockito.Mockito.mock(PrivateMessagePublisher.class);
    private final ChatApplicationServiceImpl service =
        new ChatApplicationServiceImpl(privateMessagePublisher);

    @Test
    void delegatesPrivateMessagePublishing() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        PrivateMessageRequest request = new PrivateMessageRequest(
            recipientId,
            "hello"
        );

        service.handlePrivateMessage(senderId, request);

        verify(privateMessagePublisher).publish(senderId, request);
    }
}
