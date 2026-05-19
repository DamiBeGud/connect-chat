package com.connectchat.identity.config;

import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "identity")
public record InternalServiceClientProperties(
    Map<String, InternalServiceClient> internalClients
) {
    public Optional<InternalServiceClient> findByClientId(String clientId) {
        if (internalClients == null || internalClients.isEmpty()) {
            return Optional.empty();
        }

        return internalClients
            .values()
            .stream()
            .filter(client -> client.clientId().equals(clientId))
            .findFirst();
    }

    public record InternalServiceClient(
        String clientId,
        String clientSecret
    ) {}
}
