package com.connectchat.chat.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.chat.api.response.PrivateMessageResponse;
import com.connectchat.chat.client.PresenceClient;
import com.connectchat.chat.client.response.PresenceResponse;
import com.connectchat.chat.client.response.PresenceSessionResponse;
import com.connectchat.chat.config.WebSocketDeliveryProperties;
import com.connectchat.chat.entity.WebSocketDeliveryTask;
import com.connectchat.chat.entity.WebSocketDeliveryTaskType;
import com.connectchat.chat.service.WebSocketDeliveryTaskService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WebSocketDeliveryFanoutServiceTest {

    private final PresenceClient presenceClient = org.mockito.Mockito.mock(
        PresenceClient.class
    );
    private final WebSocketDeliveryTaskService deliveryTaskService =
        org.mockito.Mockito.mock(WebSocketDeliveryTaskService.class);
    private final WebSocketDeliveryPayloadConverter payloadConverter =
        org.mockito.Mockito.mock(WebSocketDeliveryPayloadConverter.class);
    private final WebSocketDeliveryProperties properties =
        new WebSocketDeliveryProperties(
            10,
            500L,
            Duration.ofMinutes(5),
            Duration.ofSeconds(30),
            5000L
        );
    private final Clock clock = Clock.fixed(
        Instant.parse("2026-05-06T10:15:30Z"),
        ZoneOffset.UTC
    );
    private final WebSocketDeliveryFanoutService service =
        new WebSocketDeliveryFanoutService(
            presenceClient,
            deliveryTaskService,
            payloadConverter,
            properties,
            clock
        );

    @Test
    void createsPrivateMessageTaskForEachActiveSessionAcrossInstances() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        PrivateMessageResponse response = new PrivateMessageResponse(
            messageId,
            senderId,
            "+49111111111",
            recipientId,
            "hello",
            Instant.now(clock)
        );
        when(payloadConverter.serialize(response)).thenReturn("{\"message\":true}");
        when(presenceClient.getPresence(List.of(senderId, recipientId))).thenReturn(
            List.of(
                presence(senderId, session(senderId, "sender-session", "chat:a")),
                presence(
                    recipientId,
                    session(recipientId, "recipient-session", "chat:b")
                )
            )
        );

        service.createPrivateMessageTasks(response);

        List<WebSocketDeliveryTask> tasks = capturedTasks();
        assertThat(tasks).hasSize(2);
        assertThat(tasks)
            .extracting(WebSocketDeliveryTask::getTargetInstanceId)
            .containsExactly("chat:a", "chat:b");
        assertThat(tasks)
            .allSatisfy(task -> {
                assertThat(task.getSourceEventId()).isEqualTo(messageId);
                assertThat(task.getType())
                    .isEqualTo(WebSocketDeliveryTaskType.PRIVATE_MESSAGE);
                assertThat(task.getDestination())
                    .isEqualTo(
                        WebSocketDeliveryFanoutService.PRIVATE_MESSAGES_DESTINATION
                    );
                assertThat(task.getPayload()).isEqualTo("{\"message\":true}");
                assertThat(task.getExpiresAt())
                    .isEqualTo(Instant.parse("2026-05-06T10:20:30Z"));
            });
    }

    @Test
    void deduplicatesSelfMessageSessionsBeforeInsert() {
        UUID userId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        PrivateMessageResponse response = new PrivateMessageResponse(
            messageId,
            userId,
            "+49111111111",
            userId,
            "note to self",
            Instant.now(clock)
        );
        PresenceSessionResponse session = session(userId, "same-session", "chat:a");
        when(payloadConverter.serialize(response)).thenReturn("{\"self\":true}");
        when(presenceClient.getPresence(List.of(userId))).thenReturn(
            List.of(presence(userId, session, session))
        );

        service.createPrivateMessageTasks(response);

        List<WebSocketDeliveryTask> tasks = capturedTasks();
        assertThat(tasks).hasSize(1);
        assertThat(tasks.getFirst().getTargetSessionId()).isEqualTo("same-session");
    }

    @Test
    void createsPrivateMessageTaskForSpecificSession() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        PrivateMessageResponse response = new PrivateMessageResponse(
            messageId,
            senderId,
            "+49111111111",
            recipientId,
            "missed",
            Instant.now(clock)
        );
        when(payloadConverter.serialize(response)).thenReturn("{\"missed\":true}");

        service.createPrivateMessageTaskForSession(
            response,
            "recipient-session",
            "chat:local"
        );

        List<WebSocketDeliveryTask> tasks = capturedTasks();
        assertThat(tasks).hasSize(1);
        WebSocketDeliveryTask task = tasks.getFirst();
        assertThat(task.getSourceEventId()).isEqualTo(messageId);
        assertThat(task.getType()).isEqualTo(WebSocketDeliveryTaskType.PRIVATE_MESSAGE);
        assertThat(task.getTargetUserId()).isEqualTo(recipientId);
        assertThat(task.getTargetSessionId()).isEqualTo("recipient-session");
        assertThat(task.getTargetInstanceId()).isEqualTo("chat:local");
        assertThat(task.getDestination())
            .isEqualTo(WebSocketDeliveryFanoutService.PRIVATE_MESSAGES_DESTINATION);
        assertThat(task.getPayload()).isEqualTo("{\"missed\":true}");
        assertThat(task.getExpiresAt())
            .isEqualTo(Instant.parse("2026-05-06T10:20:30Z"));
    }

    @SuppressWarnings("unchecked")
    private List<WebSocketDeliveryTask> capturedTasks() {
        ArgumentCaptor<Collection<WebSocketDeliveryTask>> captor =
            ArgumentCaptor.forClass(Collection.class);
        verify(deliveryTaskService).createTasks(captor.capture());
        return new ArrayList<>(captor.getValue());
    }

    private PresenceResponse presence(
        UUID userId,
        PresenceSessionResponse... sessions
    ) {
        return new PresenceResponse(
            userId,
            "ONLINE",
            List.of(sessions),
            Instant.now(clock)
        );
    }

    private PresenceSessionResponse session(
        UUID userId,
        String sessionId,
        String instanceId
    ) {
        return new PresenceSessionResponse(
            userId,
            sessionId,
            instanceId,
            Instant.now(clock),
            Instant.now(clock)
        );
    }
}
