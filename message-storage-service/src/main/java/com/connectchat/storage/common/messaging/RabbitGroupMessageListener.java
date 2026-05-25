package com.connectchat.storage.common.messaging;

import com.connectchat.storage.service.MessageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitGroupMessageListener {

    private final MessageStorageService messageStorageService;

    @RabbitListener(queues = "${message-storage.messaging.group-message-queue}")
    public void handleGroupMessage(GroupMessageEvent event) {
        messageStorageService.storeGroupMessage(event);
    }
}
