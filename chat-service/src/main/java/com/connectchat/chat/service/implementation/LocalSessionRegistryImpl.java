package com.connectchat.chat.service.implementation;

import com.connectchat.chat.service.LocalSession;
import com.connectchat.chat.service.LocalSessionRegistry;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalSessionRegistryImpl implements LocalSessionRegistry {

    private final Clock clock;
    private final Map<String, LocalSession> sessionsById =
        new ConcurrentHashMap<>();

    @Override
    public void addSession(UUID userId, String sessionId) {
        sessionsById.put(
            sessionId,
            new LocalSession(userId, sessionId, Instant.now(clock))
        );
    }

    @Override
    public Optional<LocalSession> removeSession(String sessionId) {
        return Optional.ofNullable(sessionsById.remove(sessionId));
    }

    @Override
    public Optional<LocalSession> findSession(String sessionId) {
        return Optional.ofNullable(sessionsById.get(sessionId));
    }

    @Override
    public boolean hasLocalSession(UUID userId) {
        return sessionsById
            .values()
            .stream()
            .anyMatch(session -> session.userId().equals(userId));
    }

    @Override
    public boolean hasLocalSession(UUID userId, String sessionId) {
        return findSession(sessionId)
            .map(session -> session.userId().equals(userId))
            .orElse(false);
    }
}
