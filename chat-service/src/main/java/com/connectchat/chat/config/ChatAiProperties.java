package com.connectchat.chat.config;

import java.util.UUID;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat.ai")
public record ChatAiProperties(
    UUID botUserId,
    String botInboxExchange,
    String botInboxRoutingKey,
    String aiReplyCommandQueue,
    String aiReplyExchange,
    String aiReplyRoutingKey
) {}
