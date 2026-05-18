package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.response.PrivateMessageResponse;
import com.connectchat.chat.common.messaging.PrivateMessageCommand;
import com.connectchat.chat.service.MessageDeliveryService;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageDeliveryServiceImpl implements MessageDeliveryService {

    public static final String PRIVATE_MESSAGES_DESTINATION =
        "/queue/private-messages";

    private final SimpMessagingTemplate messagingTemplate;
    private final Clock clock;

    @Override
    public void deliver(PrivateMessageCommand command) {
        PrivateMessageResponse message = new PrivateMessageResponse(
            command.messageId() != null ? command.messageId() : UUID.randomUUID(),
            command.senderId(),
            command.recipientId(),
            command.content(),
            command.occurredAt() != null ? command.occurredAt() : Instant.now(clock)
        );

        messagingTemplate.convertAndSendToUser(
            command.recipientId().toString(),
            PRIVATE_MESSAGES_DESTINATION,
            message
        );
        messagingTemplate.convertAndSendToUser(
            command.senderId().toString(),
            PRIVATE_MESSAGES_DESTINATION,
            message
        );
    }
}
