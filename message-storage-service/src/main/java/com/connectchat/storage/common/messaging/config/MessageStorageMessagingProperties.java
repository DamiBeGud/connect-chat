package com.connectchat.storage.common.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "message-storage.messaging")
public record MessageStorageMessagingProperties(
    String privateMessageQueue,
    String privateMessageExchange,
    String privateMessageRoutingKey,
    String statusRequestQueue,
    String statusRequestExchange,
    String statusRequestRoutingKey,
    String statusConfirmedExchange,
    String statusConfirmedRoutingKey,
    int inboxBatchSize,
    long inboxProcessingDelay
) {}
