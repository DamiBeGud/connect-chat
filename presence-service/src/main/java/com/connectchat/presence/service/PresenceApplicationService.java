package com.connectchat.presence.service;

import com.connectchat.presence.api.request.RegisterPresenceSessionRequest;
import com.connectchat.presence.api.response.PresenceResponse;
import java.util.List;
import java.util.UUID;

public interface PresenceApplicationService {
    void registerSession(RegisterPresenceSessionRequest request);

    void removeSession(String sessionId);

    PresenceResponse getUserPresence(UUID userId);

    List<PresenceResponse> getUsersPresence(List<UUID> userIds);

    boolean isUserOnline(UUID userId);
}
