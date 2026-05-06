package com.connectchat.chat.api;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.api.response.PrivateMessageResponse;
import com.connectchat.chat.service.ChatApplicationService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatApplicationService chatApplicationService;

    @MessageMapping("/chat.private")
    public PrivateMessageResponse sendPrivateMessage(
        @Valid @Payload PrivateMessageRequest request,
        Principal principal
    ) {
        return chatApplicationService.handlePrivateMessage(
            UUID.fromString(principal.getName()),
            request
        );
    }
}
