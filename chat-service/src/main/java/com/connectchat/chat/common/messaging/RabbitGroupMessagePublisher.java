package com.connectchat.chat.common.messaging;

import com.connectchat.chat.common.messaging.config.ChatMessagingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitGroupMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ChatMessagingProperties properties;

    public void publish(GroupMessageEvent event) {
        rabbitTemplate.convertAndSend(
            properties.groupMessageExchange(),
            properties.groupMessageRoutingKey(),
            event
        );
    }
}
