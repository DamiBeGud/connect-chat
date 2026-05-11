package com.connectchat.chat.common.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat.messaging")
public record ChatMessagingProperties(
    String privateMessageQueue,
    String privateMessageExchange,
    String privateMessageRoutingKey
) {}
