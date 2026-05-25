package com.connectchat.chat.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import com.connectchat.chat.api.response.GroupMessageResponse;
import com.connectchat.chat.api.response.PrivateMessageResponse;
import com.connectchat.chat.api.response.PrivateMessageStatusResponse;
import com.connectchat.chat.common.messaging.PrivateMessageStatus;
import com.connectchat.chat.entity.WebSocketDeliveryTask;
import com.connectchat.chat.entity.WebSocketDeliveryTaskType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WebSocketDeliveryPayloadConverterTest {

    private final WebSocketDeliveryPayloadConverter converter =
        new WebSocketDeliveryPayloadConverter(
            new ObjectMapper().findAndRegisterModules()
        );

    @Test
    void serializesAndDeserializesPrivateMessagePayloadWithInstant() {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        Instant sentAt = Instant.parse("2026-05-20T19:26:07Z");
        PrivateMessageResponse response = new PrivateMessageResponse(
            messageId,
            senderId,
            "+49111111111",
            recipientId,
            "hello",
            sentAt
        );

        String payload = converter.serialize(response);
        Object decoded = converter.deserialize(
            task(WebSocketDeliveryTaskType.PRIVATE_MESSAGE, payload)
        );

        assertThat(decoded).isEqualTo(response);
    }

    @Test
    void serializesAndDeserializesPrivateMessageStatusPayloadWithInstant() {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-05-20T19:26:08Z");
        PrivateMessageStatusResponse response = new PrivateMessageStatusResponse(
            messageId,
            senderId,
            recipientId,
            PrivateMessageStatus.DELIVERED,
            actorUserId,
            occurredAt
        );

        String payload = converter.serialize(response);
        Object decoded = converter.deserialize(
            task(WebSocketDeliveryTaskType.PRIVATE_MESSAGE_STATUS, payload)
        );

        assertThat(decoded).isEqualTo(response);
    }

    @Test
    void serializesAndDeserializesGroupMessagePayloadWithInstant() {
        UUID messageId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        Instant sentAt = Instant.parse("2026-05-20T19:26:09Z");
        GroupMessageResponse response = new GroupMessageResponse(
            messageId,
            groupId,
            senderId,
            "+49111111111",
            "hello group",
            sentAt
        );

        String payload = converter.serialize(response);
        Object decoded = converter.deserialize(
            task(WebSocketDeliveryTaskType.GROUP_MESSAGE, payload)
        );

        assertThat(decoded).isEqualTo(response);
    }

    private WebSocketDeliveryTask task(
        WebSocketDeliveryTaskType type,
        String payload
    ) {
        return WebSocketDeliveryTask.builder()
            .id(UUID.randomUUID())
            .sourceEventId(UUID.randomUUID())
            .type(type)
            .targetUserId(UUID.randomUUID())
            .targetSessionId("session-1")
            .targetInstanceId("chat-service:pod-1")
            .destination("/queue/private-messages")
            .payload(payload)
            .expiresAt(Instant.parse("2026-05-20T19:31:07Z"))
            .build();
    }
}
