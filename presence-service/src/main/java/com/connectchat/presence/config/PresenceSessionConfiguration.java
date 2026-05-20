package com.connectchat.presence.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PresenceSessionProperties.class)
public class PresenceSessionConfiguration {}
