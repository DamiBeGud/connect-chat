package com.connectchat.chat.client;

import com.connectchat.chat.client.response.PresenceResponse;
import java.util.List;
import java.util.UUID;

public interface PresenceClient {
    void registerSession(UUID userId, String sessionId, String instanceId);

    void removeSession(String sessionId);

    PresenceResponse getPresence(UUID userId);

    List<PresenceResponse> getPresence(List<UUID> userIds);

    boolean isUserOnline(UUID userId);
}
