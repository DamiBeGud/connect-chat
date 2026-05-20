package com.connectchat.chat.service.implementation;

import com.connectchat.chat.config.WebSocketDeliveryProperties;
import com.connectchat.chat.entity.WebSocketDeliveryTask;
import com.connectchat.chat.repository.WebSocketDeliveryTaskRepository;
import com.connectchat.chat.service.WebSocketDeliveryTaskService;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebSocketDeliveryTaskServiceImpl
    implements WebSocketDeliveryTaskService {

    private final WebSocketDeliveryTaskRepository repository;
    private final WebSocketDeliveryProperties properties;
    private final Clock clock;

    @Override
    @Transactional
    public int createTasks(Collection<WebSocketDeliveryTask> tasks) {
        int inserted = 0;
        for (WebSocketDeliveryTask task : tasks) {
            inserted += repository.insertIfAbsent(
                UUID.randomUUID(),
                task.getSourceEventId(),
                task.getType().name(),
                task.getTargetUserId(),
                task.getTargetSessionId(),
                task.getTargetInstanceId(),
                task.getDestination(),
                task.getPayload(),
                task.getExpiresAt()
            );
        }

        return inserted;
    }

    @Override
    @Transactional
    public List<WebSocketDeliveryTask> claimNextBatch(
        String instanceId,
        int batchSize
    ) {
        List<WebSocketDeliveryTask> tasks = repository.findBatchForProcessing(
            instanceId,
            batchSize
        );
        tasks.forEach(WebSocketDeliveryTask::markProcessing);
        return new ArrayList<>(tasks);
    }

    @Override
    @Transactional
    public void markProcessed(UUID id) {
        repository.findById(id).ifPresent(WebSocketDeliveryTask::markProcessed);
    }

    @Override
    @Transactional
    public void markFailed(UUID id, String reason) {
        repository.findById(id).ifPresent(task -> task.markFailed(reason));
    }

    @Override
    @Transactional
    public void markExpired(UUID id, String reason) {
        repository.findById(id).ifPresent(task -> task.markExpired(reason));
    }

    @Override
    @Transactional
    public int expireStaleTasks() {
        int expired = repository.expireDueTasks();
        Instant lockedBefore = Instant
            .now(clock)
            .minus(properties.staleProcessingTimeout());
        return expired + repository.resetStaleProcessingTasks(lockedBefore);
    }
}
