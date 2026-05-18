package com.connectchat.storage.common.messaging;

import com.connectchat.storage.service.StorageStatusInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMessageStatusListener {

    private final StorageStatusInboxService storageStatusInboxService;

    @RabbitListener(queues = "${message-storage.messaging.status-request-queue}")
    public void handleStatusEvent(MessageStatusEvent event) {
        storageStatusInboxService.enqueue(event);
    }
}
