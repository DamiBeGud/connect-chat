package com.connectchat.chat.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.chat.config.WebSocketDeliveryProperties;
import com.connectchat.chat.entity.WebSocketDeliveryTask;
import com.connectchat.chat.entity.WebSocketDeliveryTaskStatus;
import com.connectchat.chat.entity.WebSocketDeliveryTaskType;
import com.connectchat.chat.repository.WebSocketDeliveryTaskRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WebSocketDeliveryTaskServiceImplTest {

    private final WebSocketDeliveryTaskRepository repository =
        org.mockito.Mockito.mock(WebSocketDeliveryTaskRepository.class);
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
    private final WebSocketDeliveryTaskServiceImpl service =
        new WebSocketDeliveryTaskServiceImpl(repository, properties, clock);

    @Test
    void insertsTasksWithDuplicateSafeRepositoryMethod() {
        WebSocketDeliveryTask task = task();
        when(
            repository.insertIfAbsent(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(task.getSourceEventId()),
                org.mockito.ArgumentMatchers.eq(task.getType().name()),
                org.mockito.ArgumentMatchers.eq(task.getTargetUserId()),
                org.mockito.ArgumentMatchers.eq(task.getTargetSessionId()),
                org.mockito.ArgumentMatchers.eq(task.getTargetInstanceId()),
                org.mockito.ArgumentMatchers.eq(task.getDestination()),
                org.mockito.ArgumentMatchers.eq(task.getPayload()),
                org.mockito.ArgumentMatchers.eq(task.getExpiresAt())
            )
        ).thenReturn(1);

        assertThat(service.createTasks(List.of(task))).isEqualTo(1);
    }

    @Test
    void claimNextBatchMarksTasksProcessing() {
        WebSocketDeliveryTask task = task();
        when(repository.findBatchForProcessing("chat-service:pod-1", 10)).thenReturn(
            List.of(task)
        );

        List<WebSocketDeliveryTask> claimed = service.claimNextBatch(
            "chat-service:pod-1",
            10
        );

        assertThat(claimed).containsExactly(task);
        assertThat(task.getStatus())
            .isEqualTo(WebSocketDeliveryTaskStatus.PROCESSING);
        assertThat(task.getAttempts()).isEqualTo(1);
    }

    @Test
    void markExpiredSetsTerminalExpiredStatus() {
        WebSocketDeliveryTask task = task();
        when(repository.findById(task.getId())).thenReturn(Optional.of(task));

        service.markExpired(task.getId(), "stale session");

        assertThat(task.getStatus()).isEqualTo(WebSocketDeliveryTaskStatus.EXPIRED);
        assertThat(task.getErrorMessage()).isEqualTo("stale session");
    }

    @Test
    void expireStaleTasksExpiresDueAndResetsOldProcessingLocks() {
        when(repository.expireDueTasks()).thenReturn(2);
        when(
            repository.resetStaleProcessingTasks(
                Instant.parse("2026-05-06T10:15:00Z")
            )
        ).thenReturn(1);

        assertThat(service.expireStaleTasks()).isEqualTo(3);

        verify(repository).expireDueTasks();
        verify(repository)
            .resetStaleProcessingTasks(
                Instant.parse("2026-05-06T10:15:00Z")
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
            .expiresAt(Instant.parse("2026-05-06T10:20:30Z"))
            .build();
    }
}
