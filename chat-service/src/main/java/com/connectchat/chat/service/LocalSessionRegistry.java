package com.connectchat.chat.service;

import java.util.Optional;
import java.util.UUID;

public interface LocalSessionRegistry {
    void addSession(UUID userId, String sessionId);

    Optional<LocalSession> removeSession(String sessionId);

    Optional<LocalSession> findSession(String sessionId);

    boolean hasLocalSession(UUID userId);

    boolean hasLocalSession(UUID userId, String sessionId);
}
