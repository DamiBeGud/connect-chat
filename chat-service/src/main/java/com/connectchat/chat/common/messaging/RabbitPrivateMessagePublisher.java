package com.connectchat.chat.common.messaging;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.common.messaging.config.ChatMessagingProperties;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitPrivateMessagePublisher implements PrivateMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ChatMessagingProperties chatMessagingProperties;

    @Override
    public void publish(UUID senderId, PrivateMessageRequest request) {
        PrivateMessageCommand command = new PrivateMessageCommand(
            senderId,
            request.recipientId(),
            request.content()
        );

        rabbitTemplate.convertAndSend(
            chatMessagingProperties.privateMessageExchange(),
            chatMessagingProperties.privateMessageRoutingKey(),
            command
        );
        log.info(
            "Published private chat message senderId={} recipientId={} exchange={} routingKey={}",
            senderId,
            request.recipientId(),
            chatMessagingProperties.privateMessageExchange(),
            chatMessagingProperties.privateMessageRoutingKey()
        );
    }
}
