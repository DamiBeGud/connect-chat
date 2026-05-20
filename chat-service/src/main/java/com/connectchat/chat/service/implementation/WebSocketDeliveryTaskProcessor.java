package com.connectchat.chat.service.implementation;

import com.connectchat.chat.config.ChatInstanceInfo;
import com.connectchat.chat.config.WebSocketDeliveryProperties;
import com.connectchat.chat.entity.WebSocketDeliveryTask;
import com.connectchat.chat.service.LocalSessionRegistry;
import com.connectchat.chat.service.WebSocketDeliveryTaskService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketDeliveryTaskProcessor {

    private final WebSocketDeliveryTaskService deliveryTaskService;
    private final WebSocketLocalDeliveryService localDeliveryService;
    private final LocalSessionRegistry localSessionRegistry;
    private final ChatInstanceInfo chatInstanceInfo;
    private final WebSocketDeliveryProperties properties;

    @Scheduled(
        fixedDelayString = "${chat.websocket-delivery.processing-delay:500}"
    )
    public void processWebSocketDeliveryTasks() {
        List<WebSocketDeliveryTask> tasks = deliveryTaskService.claimNextBatch(
            chatInstanceInfo.getInstanceId(),
            properties.batchSize()
        );

        for (WebSocketDeliveryTask task : tasks) {
            process(task);
        }
    }

    @Scheduled(fixedDelayString = "${chat.websocket-delivery.cleanup-delay:5000}")
    public void cleanupStaleTasks() {
        deliveryTaskService.expireStaleTasks();
    }

    private void process(WebSocketDeliveryTask task) {
        if (
            !localSessionRegistry.hasLocalSession(
                task.getTargetUserId(),
                task.getTargetSessionId()
            )
        ) {
            deliveryTaskService.markExpired(
                task.getId(),
                "Target websocket session is no longer local"
            );
            return;
        }

        try {
            localDeliveryService.deliver(task);
            deliveryTaskService.markProcessed(task.getId());
        } catch (RuntimeException exception) {
            deliveryTaskService.markFailed(task.getId(), exception.getMessage());
            log.warn(
                "Failed to deliver websocket task id={} targetSessionId={}",
                task.getId(),
                task.getTargetSessionId(),
                exception
            );
        }
    }
}
