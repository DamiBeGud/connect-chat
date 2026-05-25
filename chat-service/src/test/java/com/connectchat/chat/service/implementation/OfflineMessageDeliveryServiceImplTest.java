package com.connectchat.chat.service.implementation;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.chat.api.response.PrivateMessageResponse;
import com.connectchat.chat.client.IdentityUserClient;
import com.connectchat.chat.client.MessageStorageClient;
import com.connectchat.chat.client.response.IdentityUserResponse;
import com.connectchat.chat.client.response.UndeliveredMessageResponse;
import com.connectchat.chat.config.ChatInstanceInfo;
import com.connectchat.chat.config.OfflineMessageDeliveryProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OfflineMessageDeliveryServiceImplTest {

    private final MessageStorageClient messageStorageClient =
        org.mockito.Mockito.mock(MessageStorageClient.class);
    private final IdentityUserClient identityUserClient = org.mockito.Mockito.mock(
        IdentityUserClient.class
    );
    private final WebSocketDeliveryFanoutService fanoutService =
        org.mockito.Mockito.mock(WebSocketDeliveryFanoutService.class);
    private final ChatInstanceInfo chatInstanceInfo = new ChatInstanceInfo(
        "pod-1",
        "chat-service"
    );
    private final OfflineMessageDeliveryProperties properties =
        new OfflineMessageDeliveryProperties(50, Duration.ofSeconds(3));
    private final OfflineMessageDeliveryServiceImpl service =
        new OfflineMessageDeliveryServiceImpl(
            messageStorageClient,
            identityUserClient,
            fanoutService,
            chatInstanceInfo,
            properties
        );

    @Test
    void createsTargetedTasksForUndeliveredMessages() {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        Instant sentAt = Instant.parse("2026-05-06T10:15:30Z");
        when(messageStorageClient.getUndeliveredMessages(recipientId, 50))
            .thenReturn(
                List.of(
                    new UndeliveredMessageResponse(
                        messageId,
                        senderId,
                        recipientId,
                        "missed",
                        "SENT",
                        sentAt
                    )
                )
            );
        when(identityUserClient.getUserById(senderId)).thenReturn(
            new IdentityUserResponse(senderId, "+49111111111")
        );

        service.deliverPendingMessages(recipientId, "session-1");

        verify(fanoutService)
            .createPrivateMessageTaskForSession(
                argThat((PrivateMessageResponse message) ->
                    messageId.equals(message.messageId()) &&
                    senderId.equals(message.senderId()) &&
                    "+49111111111".equals(message.senderPhoneNumber()) &&
                    recipientId.equals(message.recipientId()) &&
                    "missed".equals(message.content()) &&
                    sentAt.equals(message.sentAt())
                ),
                org.mockito.ArgumentMatchers.eq("session-1"),
                org.mockito.ArgumentMatchers.eq("chat-service:pod-1")
            );
    }
}
