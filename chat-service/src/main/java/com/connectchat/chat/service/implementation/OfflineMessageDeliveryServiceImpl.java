package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.response.GroupMessageResponse;
import com.connectchat.chat.api.response.PrivateMessageResponse;
import com.connectchat.chat.client.IdentityUserClient;
import com.connectchat.chat.client.MessageStorageClient;
import com.connectchat.chat.client.response.IdentityUserResponse;
import com.connectchat.chat.client.response.UndeliveredMessageResponse;
import com.connectchat.chat.config.ChatInstanceInfo;
import com.connectchat.chat.config.OfflineMessageDeliveryProperties;
import com.connectchat.chat.service.OfflineMessageDeliveryService;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfflineMessageDeliveryServiceImpl
    implements OfflineMessageDeliveryService {

    private final MessageStorageClient messageStorageClient;
    private final IdentityUserClient identityUserClient;
    private final WebSocketDeliveryFanoutService fanoutService;
    private final ChatInstanceInfo chatInstanceInfo;
    private final OfflineMessageDeliveryProperties properties;

    @Override
    public void deliverPendingMessages(UUID userId, String sessionId) {
        for (UndeliveredMessageResponse message : messageStorageClient.getUndeliveredMessages(
            userId,
            properties.batchSize()
        )) {
            IdentityUserResponse sender = identityUserClient.getUserById(
                message.senderId()
            );
            if ("GROUP".equals(message.messageType())) {
                fanoutService.createGroupMessageTaskForSession(
                    toGroupMessageResponse(message, sender),
                    userId,
                    sessionId,
                    chatInstanceInfo.getInstanceId()
                );
            } else {
                fanoutService.createPrivateMessageTaskForSession(
                    toPrivateMessageResponse(message, sender),
                    sessionId,
                    chatInstanceInfo.getInstanceId()
                );
            }
        }
    }

    @Override
    public void scheduleDelayedDelivery(UUID userId, String sessionId) {
        CompletableFuture
            .runAsync(
                () -> deliverPendingMessages(userId, sessionId),
                CompletableFuture.delayedExecutor(
                    properties.retryDelay().toMillis(),
                    TimeUnit.MILLISECONDS
                )
            )
            .exceptionally(exception -> {
                log.warn(
                    "Delayed offline message delivery failed userId={} sessionId={}",
                    userId,
                    sessionId,
                    exception
                );
                return null;
            });
    }

    private PrivateMessageResponse toPrivateMessageResponse(
        UndeliveredMessageResponse message,
        IdentityUserResponse sender
    ) {
        return new PrivateMessageResponse(
            message.messageId(),
            message.senderId(),
            sender.phoneNumber(),
            message.recipientId(),
            message.content(),
            message.sentAt()
        );
    }

    private GroupMessageResponse toGroupMessageResponse(
        UndeliveredMessageResponse message,
        IdentityUserResponse sender
    ) {
        return new GroupMessageResponse(
            message.messageId(),
            message.groupId(),
            message.senderId(),
            sender.phoneNumber(),
            message.content(),
            message.sentAt()
        );
    }
}
