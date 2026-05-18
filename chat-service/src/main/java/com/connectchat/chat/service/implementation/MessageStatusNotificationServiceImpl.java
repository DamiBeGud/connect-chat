package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.response.PrivateMessageStatusResponse;
import com.connectchat.chat.entity.MessageStatusInboxEvent;
import com.connectchat.chat.service.MessageStatusNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageStatusNotificationServiceImpl
    implements MessageStatusNotificationService {

    public static final String PRIVATE_MESSAGE_STATUS_DESTINATION =
        "/queue/private-message-status";

    private final SimpMessagingTemplate messagingTemplate;

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

        messagingTemplate.convertAndSendToUser(
            event.getSenderId().toString(),
            PRIVATE_MESSAGE_STATUS_DESTINATION,
            response
        );
        messagingTemplate.convertAndSendToUser(
            event.getRecipientId().toString(),
            PRIVATE_MESSAGE_STATUS_DESTINATION,
            response
        );
    }
}
