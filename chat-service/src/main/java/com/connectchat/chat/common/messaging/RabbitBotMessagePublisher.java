package com.connectchat.chat.common.messaging;

import com.connectchat.chat.config.ChatAiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitBotMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ChatAiProperties properties;

    public void publish(BotMessageCommand command) {
        rabbitTemplate.convertAndSend(
            properties.botInboxExchange(),
            properties.botInboxRoutingKey(),
            command
        );
    }
}
