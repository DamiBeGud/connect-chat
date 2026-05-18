package com.connectchat.chat.api;

import static org.mockito.Mockito.verify;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.api.request.PrivateMessageStatusRequest;
import com.connectchat.chat.common.messaging.PrivateMessageStatus;
import com.connectchat.chat.service.ChatApplicationService;
import java.security.Principal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatWebSocketControllerTest {

    private final ChatApplicationService chatApplicationService = org.mockito.Mockito.mock(
        ChatApplicationService.class
    );
    private final ChatWebSocketController controller =
        new ChatWebSocketController(chatApplicationService);

    @Test
    void sendsPrivateMessageAsAuthenticatedUser() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        PrivateMessageRequest request = new PrivateMessageRequest(
            recipientId,
            "hello"
        );
        Principal principal = senderId::toString;

        controller.sendPrivateMessage(request, principal);

        verify(chatApplicationService).handlePrivateMessage(senderId, request);
    }

    @Test
    void acknowledgesDeliveredMessageAsAuthenticatedRecipient() {
        UUID recipientId = UUID.randomUUID();
        PrivateMessageStatusRequest request = new PrivateMessageStatusRequest(
            UUID.randomUUID()
        );
        Principal principal = recipientId::toString;

        controller.acknowledgeDelivered(request, principal);

        verify(chatApplicationService).handlePrivateMessageStatus(
            recipientId,
            request.messageId(),
            PrivateMessageStatus.DELIVERED
        );
    }

    @Test
    void acknowledgesReadMessageAsAuthenticatedRecipient() {
        UUID recipientId = UUID.randomUUID();
        PrivateMessageStatusRequest request = new PrivateMessageStatusRequest(
            UUID.randomUUID()
        );
        Principal principal = recipientId::toString;

        controller.acknowledgeRead(request, principal);

        verify(chatApplicationService).handlePrivateMessageStatus(
            recipientId,
            request.messageId(),
            PrivateMessageStatus.READ
        );
    }
}
