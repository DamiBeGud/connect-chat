package com.connectchat.chat.service;

import java.util.UUID;

public interface WebSocketSessionLifecycleService {
    void registerSession(UUID userId, String sessionId);

    void removeSession(String sessionId);
}
