package com.connectchat.chat.service;

import com.connectchat.chat.api.request.GroupMessageRequest;
import com.connectchat.chat.api.request.GroupMessageStatusRequest;
import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.common.messaging.PrivateMessageStatus;
import java.util.UUID;

public interface ChatApplicationService {
    void handlePrivateMessage(
        UUID senderId,
        PrivateMessageRequest request
    );

    void handlePrivateMessageStatus(
        UUID actorUserId,
        UUID messageId,
        PrivateMessageStatus status
    );

    void handleGroupMessage(UUID senderId, GroupMessageRequest request);

    void handleGroupMessageStatus(
        UUID actorUserId,
        GroupMessageStatusRequest request,
        PrivateMessageStatus status
    );
}
