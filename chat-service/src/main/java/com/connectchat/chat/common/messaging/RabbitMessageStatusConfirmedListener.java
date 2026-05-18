package com.connectchat.chat.common.messaging;

import com.connectchat.chat.service.MessageStatusInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMessageStatusConfirmedListener {

    private final MessageStatusInboxService messageStatusInboxService;

    @RabbitListener(queues = "${chat.messaging.status-confirmed-queue}")
    public void handleConfirmedStatus(MessageStatusEvent event) {
        messageStatusInboxService.enqueue(event);
    }
}
