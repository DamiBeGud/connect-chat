package com.connectchat.chat.service.implementation;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.chat.config.ChatInstanceInfo;
import com.connectchat.chat.config.WebSocketDeliveryProperties;
import com.connectchat.chat.entity.WebSocketDeliveryTask;
import com.connectchat.chat.entity.WebSocketDeliveryTaskType;
import com.connectchat.chat.service.LocalSessionRegistry;
import com.connectchat.chat.service.WebSocketDeliveryTaskService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WebSocketDeliveryTaskProcessorTest {

    private final WebSocketDeliveryTaskService deliveryTaskService =
        org.mockito.Mockito.mock(WebSocketDeliveryTaskService.class);
    private final WebSocketLocalDeliveryService localDeliveryService =
        org.mockito.Mockito.mock(WebSocketLocalDeliveryService.class);
    private final LocalSessionRegistry localSessionRegistry = org.mockito.Mockito.mock(
        LocalSessionRegistry.class
    );
    private final ChatInstanceInfo chatInstanceInfo = new ChatInstanceInfo(
        "pod-1",
        "chat-service"
    );
    private final WebSocketDeliveryProperties properties =
        new WebSocketDeliveryProperties(
            10,
            500L,
            Duration.ofMinutes(5),
            Duration.ofSeconds(30),
            5000L
        );
    private final WebSocketDeliveryTaskProcessor processor =
        new WebSocketDeliveryTaskProcessor(
            deliveryTaskService,
            localDeliveryService,
            localSessionRegistry,
            chatInstanceInfo,
            properties
        );

    @Test
    void claimsOnlyCurrentInstanceTasksAndMarksValidTaskProcessed() {
        WebSocketDeliveryTask task = task();
        when(
            deliveryTaskService.claimNextBatch("chat-service:pod-1", 10)
        ).thenReturn(List.of(task));
        when(
            localSessionRegistry.hasLocalSession(
                task.getTargetUserId(),
                task.getTargetSessionId()
            )
        ).thenReturn(true);

        processor.processWebSocketDeliveryTasks();

        verify(localDeliveryService).deliver(task);
        verify(deliveryTaskService).markProcessed(task.getId());
    }

    @Test
    void expiresTaskForMissingLocalSession() {
        WebSocketDeliveryTask task = task();
        when(
            deliveryTaskService.claimNextBatch("chat-service:pod-1", 10)
        ).thenReturn(List.of(task));
        when(
            localSessionRegistry.hasLocalSession(
                task.getTargetUserId(),
                task.getTargetSessionId()
            )
        ).thenReturn(false);

        processor.processWebSocketDeliveryTasks();

        verify(localDeliveryService, never()).deliver(task);
        verify(deliveryTaskService).markExpired(
            task.getId(),
            "Target websocket session is no longer local"
        );
    }

    @Test
    void marksTaskFailedWhenLocalDeliveryFails() {
        WebSocketDeliveryTask task = task();
        when(
            deliveryTaskService.claimNextBatch("chat-service:pod-1", 10)
        ).thenReturn(List.of(task));
        when(
            localSessionRegistry.hasLocalSession(
                task.getTargetUserId(),
                task.getTargetSessionId()
            )
        ).thenReturn(true);
        doThrow(new IllegalStateException("broker unavailable"))
            .when(localDeliveryService)
            .deliver(task);

        processor.processWebSocketDeliveryTasks();

        verify(deliveryTaskService).markFailed(
            task.getId(),
            "broker unavailable"
        );
    }

    private WebSocketDeliveryTask task() {
        return WebSocketDeliveryTask.builder()
            .id(UUID.randomUUID())
            .sourceEventId(UUID.randomUUID())
            .type(WebSocketDeliveryTaskType.PRIVATE_MESSAGE)
            .targetUserId(UUID.randomUUID())
            .targetSessionId("session-1")
            .targetInstanceId("chat-service:pod-1")
            .destination(WebSocketDeliveryFanoutService.PRIVATE_MESSAGES_DESTINATION)
            .payload("{\"message\":true}")
            .expiresAt(Instant.now().plusSeconds(300))
            .build();
    }
}
