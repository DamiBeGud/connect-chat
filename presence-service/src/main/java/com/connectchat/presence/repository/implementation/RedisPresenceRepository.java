package com.connectchat.presence.repository.implementation;

import com.connectchat.presence.config.PresenceSessionProperties;
import com.connectchat.presence.entity.PresenceSession;
import com.connectchat.presence.entity.PresenceState;
import com.connectchat.presence.entity.PresenceStatus;
import com.connectchat.presence.repository.PresenceRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisPresenceRepository implements PresenceRepository {

    private static final String USER_SESSIONS_KEY_PREFIX = "presence:user:";
    private static final String USER_SESSIONS_KEY_SUFFIX = ":sessions";
    private static final String SESSION_KEY_PREFIX = "presence:session:";

    private final StringRedisTemplate redisTemplate;
    private final PresenceSessionProperties sessionProperties;

    @Override
    public void saveSession(PresenceSession session) {
        redisTemplate
            .opsForHash()
            .putAll(
                sessionKey(session.sessionId()),
                Map.of(
                    "userId",
                    session.userId().toString(),
                    "sessionId",
                    session.sessionId(),
                    "instanceId",
                    session.instanceId(),
                    "connectedAt",
                    session.connectedAt().toString(),
                    "lastHeartbeatAt",
                    session.lastHeartbeatAt().toString()
                )
            );
        redisTemplate
            .opsForSet()
            .add(userSessionsKey(session.userId()), session.sessionId());
        redisTemplate.expire(
            sessionKey(session.sessionId()),
            sessionProperties.ttl()
        );
    }

    @Override
    public void removeSession(String sessionId) {
        Map<Object, Object> sessionFields = redisTemplate
            .opsForHash()
            .entries(sessionKey(sessionId));

        if (sessionFields.isEmpty()) {
            return;
        }

        PresenceSession session = deserialize(sessionFields);
        redisTemplate.delete(sessionKey(sessionId));
        redisTemplate
            .opsForSet()
            .remove(userSessionsKey(session.userId()), sessionId);

        Long remainingSessions = redisTemplate
            .opsForSet()
            .size(userSessionsKey(session.userId()));
        if (remainingSessions != null && remainingSessions == 0) {
            redisTemplate.delete(userSessionsKey(session.userId()));
        }
    }

    @Override
    public PresenceStatus getPresence(UUID userId) {
        List<PresenceSession> sessions = activeSessions(userId);
        Instant updatedAt = sessions
            .stream()
            .map(PresenceSession::lastHeartbeatAt)
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .orElse(Instant.now());

        return new PresenceStatus(
            userId,
            sessions.isEmpty() ? PresenceState.OFFLINE : PresenceState.ONLINE,
            sessions,
            updatedAt
        );
    }

    @Override
    public List<PresenceStatus> getPresence(List<UUID> userIds) {
        return userIds.stream().map(this::getPresence).toList();
    }

    @Override
    public boolean isUserOnline(UUID userId) {
        return !activeSessions(userId).isEmpty();
    }

    private List<PresenceSession> activeSessions(UUID userId) {
        Set<String> sessionIds = redisTemplate
            .opsForSet()
            .members(userSessionsKey(userId));

        if (sessionIds == null || sessionIds.isEmpty()) {
            return List.of();
        }

        return sessionIds
            .stream()
            .map(sessionId -> resolveSession(userId, sessionId))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(PresenceSession::connectedAt))
            .toList();
    }

    private PresenceSession resolveSession(UUID userId, String sessionId) {
        Map<Object, Object> sessionFields = redisTemplate
            .opsForHash()
            .entries(sessionKey(sessionId));

        if (sessionFields.isEmpty()) {
            redisTemplate.opsForSet().remove(userSessionsKey(userId), sessionId);
            return null;
        }

        return deserialize(sessionFields);
    }

    private PresenceSession deserialize(Map<Object, Object> fields) {
        return new PresenceSession(
            UUID.fromString(requiredField(fields, "userId")),
            requiredField(fields, "sessionId"),
            requiredField(fields, "instanceId"),
            Instant.parse(requiredField(fields, "connectedAt")),
            Instant.parse(requiredField(fields, "lastHeartbeatAt"))
        );
    }

    private String requiredField(Map<Object, Object> fields, String fieldName) {
        Object value = fields.get(fieldName);
        if (value == null) {
            throw new IllegalStateException(
                "Presence session is missing field " + fieldName
            );
        }

        return value.toString();
    }

    private String userSessionsKey(UUID userId) {
        return USER_SESSIONS_KEY_PREFIX + userId + USER_SESSIONS_KEY_SUFFIX;
    }

    private String sessionKey(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }
}
