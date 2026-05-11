package com.connectchat.storage.common.messaging;

import com.connectchat.storage.service.StorageInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitPrivateMessageListener {

    private final StorageInboxService storageInboxService;

    @RabbitListener(queues = "${message-storage.messaging.private-message-queue}")
    public void handlePrivateMessage(PrivateMessageEvent event) {
        storageInboxService.enqueue(event);
    }
}
