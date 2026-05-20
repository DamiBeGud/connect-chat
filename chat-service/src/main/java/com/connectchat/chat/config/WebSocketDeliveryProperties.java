package com.connectchat.chat.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat.websocket-delivery")
public record WebSocketDeliveryProperties(
    int batchSize,
    long processingDelay,
    Duration taskTtl,
    Duration staleProcessingTimeout,
    long cleanupDelay
) {}
