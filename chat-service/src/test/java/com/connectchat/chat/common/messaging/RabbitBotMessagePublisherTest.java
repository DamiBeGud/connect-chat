package com.connectchat.chat.common.messaging;

import static org.mockito.Mockito.verify;

import com.connectchat.chat.config.ChatAiProperties;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class RabbitBotMessagePublisherTest {

    private static final UUID BOT_USER_ID = UUID.fromString(
        "00000000-0000-0000-0000-000000000001"
    );

    private final RabbitTemplate rabbitTemplate = org.mockito.Mockito.mock(
        RabbitTemplate.class
    );
    private final ChatAiProperties properties = new ChatAiProperties(
        BOT_USER_ID,
        "chat.bot-inbox.exchange",
        "chat.bot-inbox",
        "chat.ai-reply.commands",
        "chat.ai-reply.exchange",
        "chat.ai-reply"
    );
    private final RabbitBotMessagePublisher publisher =
        new RabbitBotMessagePublisher(rabbitTemplate, properties);

    @Test
    void publishesBotMessageCommandToConfiguredExchange() {
        BotMessageCommand command = new BotMessageCommand(
            UUID.randomUUID(),
            UUID.randomUUID(),
            BOT_USER_ID,
            "hello",
            Instant.now()
        );

        publisher.publish(command);

        verify(rabbitTemplate).convertAndSend(
            "chat.bot-inbox.exchange",
            "chat.bot-inbox",
            command
        );
    }
}
