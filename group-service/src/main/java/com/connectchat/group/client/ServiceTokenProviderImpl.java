package com.connectchat.group.client;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceTokenProviderImpl implements ServiceTokenProvider {

    private static final Duration REFRESH_SKEW = Duration.ofMinutes(1);

    private final ServiceTokenClient serviceTokenClient;
    private final Clock clock;

    private String accessToken;
    private Instant expiresAt;

    @Override
    public synchronized String authorizationHeader() {
        if (shouldRefresh()) {
            ServiceTokenResponse token = serviceTokenClient.issueServiceToken();
            accessToken = token.accessToken();
            expiresAt = token.expiresAt();
        }

        return "Bearer " + accessToken;
    }

    private boolean shouldRefresh() {
        if (accessToken == null || expiresAt == null) {
            return true;
        }

        return Instant.now(clock).plus(REFRESH_SKEW).isAfter(expiresAt);
    }
}
