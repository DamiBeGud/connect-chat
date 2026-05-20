package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.response.PrivateMessageResponse;
import com.connectchat.chat.common.messaging.PrivateMessageCommand;
import com.connectchat.chat.service.MessageDeliveryService;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageDeliveryServiceImpl implements MessageDeliveryService {

    public static final String PRIVATE_MESSAGES_DESTINATION =
        WebSocketDeliveryFanoutService.PRIVATE_MESSAGES_DESTINATION;

    private final WebSocketDeliveryFanoutService fanoutService;
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

        fanoutService.createPrivateMessageTasks(message);
    }
}
