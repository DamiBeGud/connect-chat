package com.connectchat.chat.common.messaging;

import com.connectchat.chat.common.messaging.config.ChatMessagingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMessageStatusRequestPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ChatMessagingProperties properties;

    public void publish(MessageStatusEvent event) {
        rabbitTemplate.convertAndSend(
            properties.statusRequestExchange(),
            properties.statusRequestRoutingKey(),
            event
        );
    }
}
