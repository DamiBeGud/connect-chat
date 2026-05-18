package com.connectchat.storage.common.messaging;

import com.connectchat.storage.common.messaging.config.MessageStorageMessagingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMessageStatusPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessageStorageMessagingProperties properties;

    public void publish(MessageStatusEvent event) {
        rabbitTemplate.convertAndSend(
            properties.statusConfirmedExchange(),
            properties.statusConfirmedRoutingKey(),
            event
        );
    }
}
