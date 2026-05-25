package com.connectchat.chat.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat.offline-delivery")
public record OfflineMessageDeliveryProperties(
    int batchSize,
    Duration retryDelay
) {}
