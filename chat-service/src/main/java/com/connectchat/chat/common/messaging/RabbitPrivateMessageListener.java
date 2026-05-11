package com.connectchat.chat.common.messaging;

import com.connectchat.chat.service.MessageDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitPrivateMessageListener {

    private final MessageDeliveryService messageDeliveryService;

    @RabbitListener(queues = "${chat.messaging.private-message-queue}")
    public void handlePrivateMessage(PrivateMessageCommand command) {
        messageDeliveryService.deliver(command);
    }
}
