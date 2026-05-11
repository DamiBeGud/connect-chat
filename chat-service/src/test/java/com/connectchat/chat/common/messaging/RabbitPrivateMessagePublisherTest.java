package com.connectchat.chat.common.messaging;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.common.messaging.config.ChatMessagingProperties;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class RabbitPrivateMessagePublisherTest {

    private final RabbitTemplate rabbitTemplate = org.mockito.Mockito.mock(
        RabbitTemplate.class
    );
    private final ChatMessagingProperties properties =
        new ChatMessagingProperties(
            "chat.private-message.queue",
            "chat.private-message.exchange",
            "chat.private-message"
        );
    private final RabbitPrivateMessagePublisher publisher =
        new RabbitPrivateMessagePublisher(rabbitTemplate, properties);

    @Test
    void publishesPrivateMessageCommandToRabbitMq() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        PrivateMessageRequest request = new PrivateMessageRequest(
            recipientId,
            "hello"
        );

        publisher.publish(senderId, request);

        verify(rabbitTemplate).convertAndSend(
            eq("chat.private-message.exchange"),
            eq("chat.private-message"),
            argThat((PrivateMessageCommand command) ->
                senderId.equals(command.senderId()) &&
                recipientId.equals(command.recipientId()) &&
                "hello".equals(command.content())
            )
        );
    }
}
