package com.connectchat.chat.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.api.response.PrivateMessageResponse;
import com.connectchat.chat.service.ChatApplicationService;
import java.security.Principal;
import java.time.Instant;
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
        PrivateMessageResponse response = new PrivateMessageResponse(
            UUID.randomUUID(),
            senderId,
            recipientId,
            "hello",
            Instant.parse("2026-05-06T10:15:30Z")
        );
        Principal principal = senderId::toString;

        when(
            chatApplicationService.handlePrivateMessage(senderId, request)
        ).thenReturn(response);

        PrivateMessageResponse result = controller.sendPrivateMessage(
            request,
            principal
        );

        assertThat(result).isEqualTo(response);
        verify(chatApplicationService).handlePrivateMessage(senderId, request);
    }
}
