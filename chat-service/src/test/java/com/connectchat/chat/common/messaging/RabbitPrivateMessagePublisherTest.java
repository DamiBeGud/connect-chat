package com.connectchat.chat.common.messaging;

import static org.mockito.Mockito.verify;

import com.connectchat.chat.common.messaging.config.ChatMessagingProperties;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class RabbitPrivateMessagePublisherTest {

    private final RabbitTemplate rabbitTemplate = org.mockito.Mockito.mock(
        RabbitTemplate.class
    );
    private final ChatMessagingProperties properties = new ChatMessagingProperties(
        "chat.private-message.queue",
        "chat.private-message.exchange",
        "chat.private-message",
        50,
        50,
        1000L,
        1000L
    );
    private final RabbitPrivateMessagePublisher publisher =
        new RabbitPrivateMessagePublisher(rabbitTemplate, properties);

    @Test
    void publishesPrivateMessageEventToConfiguredExchange() {
        PrivateMessageEvent event = new PrivateMessageEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "hello",
            Instant.now()
        );

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend(
            "chat.private-message.exchange",
            "chat.private-message",
            event
        );
    }
}
