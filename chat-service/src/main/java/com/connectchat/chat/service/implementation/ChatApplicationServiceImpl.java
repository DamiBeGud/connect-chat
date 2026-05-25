package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.request.GroupMessageRequest;
import com.connectchat.chat.api.request.GroupMessageStatusRequest;
import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.client.GroupClient;
import com.connectchat.chat.client.IdentityUserClient;
import com.connectchat.chat.client.MessageStorageClient;
import com.connectchat.chat.client.response.IdentityUserResponse;
import com.connectchat.chat.common.messaging.PrivateMessageStatus;
import com.connectchat.chat.entity.GroupOutboxMessage;
import com.connectchat.chat.entity.OutboxMessage;
import com.connectchat.chat.service.ChatApplicationService;
import com.connectchat.chat.service.GroupOutboxService;
import com.connectchat.chat.service.MessageStatusOutboxService;
import com.connectchat.chat.service.OutboxService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatApplicationServiceImpl implements ChatApplicationService {

    private final OutboxService outboxService;
    private final GroupOutboxService groupOutboxService;
    private final MessageStatusOutboxService messageStatusOutboxService;
    private final IdentityUserClient identityUserClient;
    private final GroupClient groupClient;
    private final MessageStorageClient messageStorageClient;

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
    public void handleGroupMessage(UUID senderId, GroupMessageRequest request) {
        List<UUID> memberIds = groupClient.getGroupMemberIds(request.groupId());
        if (!memberIds.contains(senderId)) {
            throw new AccessDeniedException(
                "Only group members can send group messages"
            );
        }

        List<UUID> recipientIds = memberIds
            .stream()
            .filter(memberId -> !memberId.equals(senderId))
            .distinct()
            .toList();

        if (recipientIds.isEmpty()) {
            throw new IllegalArgumentException(
                "Group message requires at least one recipient"
            );
        }

        groupOutboxService.enqueueGroupMessage(
            request.groupId(),
            senderId,
            recipientIds,
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

    @Override
    public void handleGroupMessageStatus(
        UUID actorUserId,
        GroupMessageStatusRequest request,
        PrivateMessageStatus status
    ) {
        GroupOutboxMessage message = groupOutboxService.requireMessage(
            request.messageId()
        );
        groupOutboxService.requireRecipient(request.messageId(), actorUserId);

        messageStorageClient.updateGroupMessageStatus(
            message.getId(),
            actorUserId,
            status
        );
    }
}
