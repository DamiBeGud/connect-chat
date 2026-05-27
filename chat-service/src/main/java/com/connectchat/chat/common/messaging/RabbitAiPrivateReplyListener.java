package com.connectchat.chat.common.messaging;

import com.connectchat.chat.config.ChatAiProperties;
import com.connectchat.chat.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitAiPrivateReplyListener {

    private final OutboxService outboxService;
    private final ChatAiProperties properties;

    @RabbitListener(queues = "${chat.ai.ai-reply-command-queue}")
    public void handleAiReply(AiPrivateReplyCommand command) {
        if (!command.senderId().equals(properties.botUserId())) {
            throw new IllegalArgumentException("AI reply sender must be bot user");
        }

        outboxService.enqueuePrivateMessage(
            command.senderId(),
            command.recipientId(),
            command.content()
        );
    }
}
