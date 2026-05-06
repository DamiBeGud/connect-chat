package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.api.response.PrivateMessageResponse;
import com.connectchat.chat.service.ChatApplicationService;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatApplicationServiceImpl implements ChatApplicationService {

    public static final String PRIVATE_MESSAGES_DESTINATION =
        "/queue/private-messages";

    private final SimpMessagingTemplate messagingTemplate;
    private final Clock clock;

    @Override
    public PrivateMessageResponse handlePrivateMessage(
        UUID senderId,
        PrivateMessageRequest request
    ) {
        PrivateMessageResponse message = new PrivateMessageResponse(
            UUID.randomUUID(),
            senderId,
            request.recipientId(),
            request.content(),
            Instant.now(clock)
        );

        messagingTemplate.convertAndSendToUser(
            request.recipientId().toString(),
            PRIVATE_MESSAGES_DESTINATION,
            message
        );
        messagingTemplate.convertAndSendToUser(
            senderId.toString(),
            PRIVATE_MESSAGES_DESTINATION,
            message
        );

        return message;
    }
}
