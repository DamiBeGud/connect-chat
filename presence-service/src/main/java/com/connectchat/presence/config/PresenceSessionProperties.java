package com.connectchat.presence.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "presence.session")
public record PresenceSessionProperties(Duration ttl) {}
