package com.connectchat.chat.common.messaging;

import com.connectchat.chat.service.InboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "chat.messaging",
    name = "private-message-listener-enabled",
    havingValue = "true"
)
public class RabbitPrivateMessageListener {

    private final InboxService inboxService;

    @RabbitListener(queues = "${chat.messaging.private-message-queue}")
    public void handlePrivateMessage(PrivateMessageEvent event) {
        inboxService.enqueue(event);
    }
}
