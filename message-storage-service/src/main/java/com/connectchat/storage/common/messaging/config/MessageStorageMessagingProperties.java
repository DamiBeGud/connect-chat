package com.connectchat.storage.common.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "message-storage.messaging")
public record MessageStorageMessagingProperties(
    String privateMessageQueue,
    String privateMessageExchange,
    String privateMessageRoutingKey,
    int inboxBatchSize,
    long inboxProcessingDelay
) {}
