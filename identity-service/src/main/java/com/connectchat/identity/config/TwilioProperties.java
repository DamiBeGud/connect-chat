package com.connectchat.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "identity.twilio")
public record TwilioProperties(
    String accountSid,
    String authToken,
    String fromPhoneNumber
) {}
