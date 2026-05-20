package com.connectchat.presence.repository.implementation;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.presence.config.PresenceSessionProperties;
import com.connectchat.presence.entity.PresenceSession;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@SuppressWarnings("unchecked")
class RedisPresenceRepositoryTest {

    private final StringRedisTemplate redisTemplate = org.mockito.Mockito.mock(
        StringRedisTemplate.class
    );
    private final HashOperations<String, Object, Object> hashOperations =
        org.mockito.Mockito.mock(HashOperations.class);
    private final SetOperations<String, String> setOperations = org.mockito.Mockito.mock(
        SetOperations.class
    );
    private final PresenceSessionProperties properties =
        new PresenceSessionProperties(Duration.ofMinutes(30));
    private final RedisPresenceRepository repository = new RedisPresenceRepository(
        redisTemplate,
        properties
    );

    @Test
    void saveSessionStoresSessionWithConfiguredTtl() {
        UUID userId = UUID.randomUUID();
        PresenceSession session = new PresenceSession(
            userId,
            "session-1",
            "chat-service:pod-1",
            Instant.parse("2026-05-06T10:15:30Z"),
            Instant.parse("2026-05-06T10:15:30Z")
        );
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        repository.saveSession(session);

        verify(hashOperations).putAll(
            eq("presence:session:session-1"),
            argThat(fields ->
                fields instanceof Map<?, ?> map &&
                userId.toString().equals(map.get("userId")) &&
                "session-1".equals(map.get("sessionId")) &&
                "chat-service:pod-1".equals(map.get("instanceId"))
            )
        );
        verify(setOperations).add(
            "presence:user:" + userId + ":sessions",
            "session-1"
        );
        verify(redisTemplate).expire(
            "presence:session:session-1",
            Duration.ofMinutes(30)
        );
    }
}
