package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.response.PrivateMessageResponse;
import com.connectchat.chat.api.response.PrivateMessageStatusResponse;
import com.connectchat.chat.client.PresenceClient;
import com.connectchat.chat.client.response.PresenceResponse;
import com.connectchat.chat.client.response.PresenceSessionResponse;
import com.connectchat.chat.config.WebSocketDeliveryProperties;
import com.connectchat.chat.entity.WebSocketDeliveryTask;
import com.connectchat.chat.entity.WebSocketDeliveryTaskType;
import com.connectchat.chat.service.WebSocketDeliveryTaskService;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketDeliveryFanoutService {

    public static final String PRIVATE_MESSAGES_DESTINATION =
        "/queue/private-messages";
    public static final String PRIVATE_MESSAGE_STATUS_DESTINATION =
        "/queue/private-message-status";

    private final PresenceClient presenceClient;
    private final WebSocketDeliveryTaskService deliveryTaskService;
    private final WebSocketDeliveryPayloadConverter payloadConverter;
    private final WebSocketDeliveryProperties properties;
    private final Clock clock;

    public int createPrivateMessageTasks(PrivateMessageResponse response) {
        return createTasksForActiveSessions(
            response.messageId(),
            WebSocketDeliveryTaskType.PRIVATE_MESSAGE,
            List.of(response.senderId(), response.recipientId()),
            PRIVATE_MESSAGES_DESTINATION,
            payloadConverter.serialize(response)
        );
    }

    public int createPrivateMessageTaskForSession(
        PrivateMessageResponse response,
        String sessionId,
        String instanceId
    ) {
        Instant expiresAt = Instant.now(clock).plus(properties.taskTtl());
        WebSocketDeliveryTask task = WebSocketDeliveryTask.builder()
            .sourceEventId(response.messageId())
            .type(WebSocketDeliveryTaskType.PRIVATE_MESSAGE)
            .targetUserId(response.recipientId())
            .targetSessionId(sessionId)
            .targetInstanceId(instanceId)
            .destination(PRIVATE_MESSAGES_DESTINATION)
            .payload(payloadConverter.serialize(response))
            .expiresAt(expiresAt)
            .build();

        return deliveryTaskService.createTasks(List.of(task));
    }

    public int createPrivateMessageStatusTasks(
        UUID sourceEventId,
        PrivateMessageStatusResponse response
    ) {
        return createTasksForActiveSessions(
            sourceEventId,
            WebSocketDeliveryTaskType.PRIVATE_MESSAGE_STATUS,
            List.of(response.senderId(), response.recipientId()),
            PRIVATE_MESSAGE_STATUS_DESTINATION,
            payloadConverter.serialize(response)
        );
    }

    private int createTasksForActiveSessions(
        UUID sourceEventId,
        WebSocketDeliveryTaskType type,
        List<UUID> targetUserIds,
        String destination,
        String payload
    ) {
        Instant expiresAt = Instant.now(clock).plus(properties.taskTtl());
        List<PresenceResponse> presence = presenceClient.getPresence(
            distinct(targetUserIds)
        );

        Collection<WebSocketDeliveryTask> tasks = presence
            .stream()
            .flatMap(response ->
                tasksForPresence(
                    sourceEventId,
                    type,
                    destination,
                    payload,
                    expiresAt,
                    response
                ).stream()
            )
            .toList();

        return deliveryTaskService.createTasks(tasks);
    }

    private List<WebSocketDeliveryTask> tasksForPresence(
        UUID sourceEventId,
        WebSocketDeliveryTaskType type,
        String destination,
        String payload,
        Instant expiresAt,
        PresenceResponse presence
    ) {
        if (presence.sessions() == null || presence.sessions().isEmpty()) {
            return List.of();
        }

        Map<String, WebSocketDeliveryTask> tasksBySession = new LinkedHashMap<>();
        for (PresenceSessionResponse session : presence.sessions()) {
            String key = session.sessionId() + ":" + destination;
            tasksBySession.putIfAbsent(
                key,
                WebSocketDeliveryTask.builder()
                    .sourceEventId(sourceEventId)
                    .type(type)
                    .targetUserId(presence.userId())
                    .targetSessionId(session.sessionId())
                    .targetInstanceId(session.instanceId())
                    .destination(destination)
                    .payload(payload)
                    .expiresAt(expiresAt)
                    .build()
            );
        }

        return new ArrayList<>(tasksBySession.values());
    }

    private List<UUID> distinct(List<UUID> values) {
        return values.stream().distinct().toList();
    }
}
