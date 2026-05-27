package com.connectchat.chat.common.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.connectchat.chat.config.ChatAiProperties;
import com.connectchat.chat.service.OutboxService;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RabbitAiPrivateReplyListenerTest {

    private static final UUID BOT_USER_ID = UUID.fromString(
        "00000000-0000-0000-0000-000000000001"
    );

    private final OutboxService outboxService = org.mockito.Mockito.mock(
        OutboxService.class
    );
    private final ChatAiProperties properties = new ChatAiProperties(
        BOT_USER_ID,
        "chat.bot-inbox.exchange",
        "chat.bot-inbox",
        "chat.ai-reply.commands",
        "chat.ai-reply.exchange",
        "chat.ai-reply"
    );
    private final RabbitAiPrivateReplyListener listener =
        new RabbitAiPrivateReplyListener(outboxService, properties);

    @Test
    void enqueuesPrivateMessageForReplyFromConfiguredBotUser() {
        UUID recipientId = UUID.randomUUID();
        AiPrivateReplyCommand command = new AiPrivateReplyCommand(
            BOT_USER_ID,
            recipientId,
            "hello human"
        );

        listener.handleAiReply(command);

        verify(outboxService)
            .enqueuePrivateMessage(BOT_USER_ID, recipientId, "hello human");
    }

    @Test
    void rejectsReplyFromNonBotSender() {
        AiPrivateReplyCommand command = new AiPrivateReplyCommand(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "spoofed"
        );

        assertThatThrownBy(() -> listener.handleAiReply(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("AI reply sender must be bot user");
        verifyNoInteractions(outboxService);
    }
}
