package com.connectchat.chat.service.implementation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.chat.api.request.GroupMessageRequest;
import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.client.GroupClient;
import com.connectchat.chat.client.IdentityUserClient;
import com.connectchat.chat.client.MessageStorageClient;
import com.connectchat.chat.client.response.IdentityUserResponse;
import com.connectchat.chat.common.messaging.PrivateMessageStatus;
import com.connectchat.chat.entity.OutboxMessage;
import com.connectchat.chat.service.GroupOutboxService;
import com.connectchat.chat.service.MessageStatusOutboxService;
import com.connectchat.chat.service.OutboxService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatApplicationServiceImplTest {

    private final OutboxService outboxService = org.mockito.Mockito.mock(
        OutboxService.class
    );
    private final GroupOutboxService groupOutboxService = org.mockito.Mockito.mock(
        GroupOutboxService.class
    );
    private final MessageStatusOutboxService messageStatusOutboxService =
        org.mockito.Mockito.mock(MessageStatusOutboxService.class);
    private final IdentityUserClient identityUserClient = org.mockito.Mockito.mock(
        IdentityUserClient.class
    );
    private final GroupClient groupClient = org.mockito.Mockito.mock(
        GroupClient.class
    );
    private final MessageStorageClient messageStorageClient = org.mockito.Mockito.mock(
        MessageStorageClient.class
    );
    private final ChatApplicationServiceImpl service =
        new ChatApplicationServiceImpl(
            outboxService,
            groupOutboxService,
            messageStatusOutboxService,
            identityUserClient,
            groupClient,
            messageStorageClient
        );

    @Test
    void enqueuesPrivateMessageInOutbox() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        PrivateMessageRequest request = new PrivateMessageRequest(
            "+49123456789",
            "hello"
        );
        when(identityUserClient.getUserByPhoneNumber("+49123456789")).thenReturn(
            new IdentityUserResponse(recipientId, "+49123456789")
        );

        service.handlePrivateMessage(senderId, request);

        verify(outboxService).enqueuePrivateMessage(senderId, recipientId, "hello");
    }

    @Test
    void enqueuesGroupMessageForOtherMembers() {
        UUID groupId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        when(groupClient.getGroupMemberIds(groupId)).thenReturn(
            List.of(senderId, recipientId)
        );

        service.handleGroupMessage(
            senderId,
            new GroupMessageRequest(groupId, "hello group")
        );

        verify(groupOutboxService)
            .enqueueGroupMessage(
                groupId,
                senderId,
                List.of(recipientId),
                "hello group"
            );
    }

    @Test
    void enqueuesDeliveredStatusForRecipientMessage() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        when(outboxService.requireMessage(messageId)).thenReturn(
            OutboxMessage.builder()
                .id(messageId)
                .senderId(senderId)
                .recipientId(recipientId)
                .content("hello")
                .build()
        );

        service.handlePrivateMessageStatus(
            recipientId,
            messageId,
            PrivateMessageStatus.DELIVERED
        );

        verify(messageStatusOutboxService).enqueue(
            messageId,
            senderId,
            recipientId,
            PrivateMessageStatus.DELIVERED,
            recipientId
        );
    }
}
