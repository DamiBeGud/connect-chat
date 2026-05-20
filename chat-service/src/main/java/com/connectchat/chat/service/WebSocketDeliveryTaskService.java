package com.connectchat.chat.service;

import com.connectchat.chat.entity.WebSocketDeliveryTask;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface WebSocketDeliveryTaskService {
    int createTasks(Collection<WebSocketDeliveryTask> tasks);

    List<WebSocketDeliveryTask> claimNextBatch(String instanceId, int batchSize);

    void markProcessed(UUID id);

    void markFailed(UUID id, String reason);

    void markExpired(UUID id, String reason);

    int expireStaleTasks();
}
