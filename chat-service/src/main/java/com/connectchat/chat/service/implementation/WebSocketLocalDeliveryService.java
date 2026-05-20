package com.connectchat.chat.service.implementation;

import com.connectchat.chat.entity.WebSocketDeliveryTask;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketLocalDeliveryService {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketDeliveryPayloadConverter payloadConverter;

    public void deliver(WebSocketDeliveryTask task) {
        SimpMessageHeaderAccessor headerAccessor =
            SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(task.getTargetSessionId());
        headerAccessor.setLeaveMutable(true);

        messagingTemplate.convertAndSendToUser(
            task.getTargetUserId().toString(),
            task.getDestination(),
            payloadConverter.deserialize(task),
            headerAccessor.getMessageHeaders()
        );
    }
}
