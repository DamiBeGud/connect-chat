package com.connectchat.chat.common.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat.messaging")
public record ChatMessagingProperties(
    String privateMessageQueue,
    String privateMessageExchange,
    String privateMessageRoutingKey,
    String statusRequestQueue,
    String statusRequestExchange,
    String statusRequestRoutingKey,
    String statusConfirmedQueue,
    String statusConfirmedExchange,
    String statusConfirmedRoutingKey,
    int outboxBatchSize,
    int inboxBatchSize,
    long outboxProcessingDelay,
    long inboxProcessingDelay
) {}
