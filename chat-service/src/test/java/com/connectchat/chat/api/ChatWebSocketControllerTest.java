package com.connectchat.chat.api;

import static org.mockito.Mockito.verify;

import com.connectchat.chat.api.request.PrivateMessageRequest;
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
}
