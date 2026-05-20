package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.response.PrivateMessageStatusResponse;
import com.connectchat.chat.entity.MessageStatusInboxEvent;
import com.connectchat.chat.service.MessageStatusNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageStatusNotificationServiceImpl
    implements MessageStatusNotificationService {

    public static final String PRIVATE_MESSAGE_STATUS_DESTINATION =
        WebSocketDeliveryFanoutService.PRIVATE_MESSAGE_STATUS_DESTINATION;

    private final WebSocketDeliveryFanoutService fanoutService;

    @Override
    public void notifyUsers(MessageStatusInboxEvent event) {
        PrivateMessageStatusResponse response = new PrivateMessageStatusResponse(
            event.getMessageId(),
            event.getSenderId(),
            event.getRecipientId(),
            event.getStatusValue(),
            event.getActorUserId(),
            event.getEventOccurredAt()
        );

        fanoutService.createPrivateMessageStatusTasks(
            event.getSourceEventId(),
            response
        );
    }
}
