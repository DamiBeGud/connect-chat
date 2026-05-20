package com.connectchat.chat.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WebSocketDeliveryProperties.class)
public class WebSocketDeliveryConfiguration {}
