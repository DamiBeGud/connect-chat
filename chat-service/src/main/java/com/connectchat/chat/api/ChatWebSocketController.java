package com.connectchat.chat.api;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.service.ChatApplicationService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatApplicationService chatApplicationService;

    @MessageMapping("/chat.private")
    public void sendPrivateMessage(
        @Valid @Payload PrivateMessageRequest request,
        Principal principal
    ) {
        UUID senderId = UUID.fromString(principal.getName());

        log.info(
            "Received private chat message senderId={} recipientId={} contentLength={}",
            senderId,
            request.recipientId(),
            request.content().length()
        );

        chatApplicationService.handlePrivateMessage(
            senderId,
            request
        );
    }
}
