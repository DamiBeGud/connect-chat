package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.client.IdentityUserClient;
import com.connectchat.chat.client.response.IdentityUserResponse;
import com.connectchat.chat.common.messaging.PrivateMessageStatus;
import com.connectchat.chat.entity.OutboxMessage;
import com.connectchat.chat.service.ChatApplicationService;
import com.connectchat.chat.service.MessageStatusOutboxService;
import com.connectchat.chat.service.OutboxService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatApplicationServiceImpl implements ChatApplicationService {

    private final OutboxService outboxService;
    private final MessageStatusOutboxService messageStatusOutboxService;
    private final IdentityUserClient identityUserClient;

    @Override
    public void handlePrivateMessage(
        UUID senderId,
        PrivateMessageRequest request
    ) {
        IdentityUserResponse recipient = identityUserClient.getUserByPhoneNumber(
            request.recipientPhoneNumber()
        );
        outboxService.enqueuePrivateMessage(
            senderId,
            recipient.userId(),
            request.content()
        );
    }

    @Override
    public void handlePrivateMessageStatus(
        UUID actorUserId,
        UUID messageId,
        PrivateMessageStatus status
    ) {
        OutboxMessage outboxMessage = outboxService.requireMessage(messageId);

        if (!actorUserId.equals(outboxMessage.getRecipientId())) {
            throw new AccessDeniedException(
                "Only the message recipient can acknowledge message status"
            );
        }

        messageStatusOutboxService.enqueue(
            messageId,
            outboxMessage.getSenderId(),
            outboxMessage.getRecipientId(),
            status,
            actorUserId
        );
    }
}
