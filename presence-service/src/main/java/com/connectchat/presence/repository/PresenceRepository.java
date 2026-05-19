package com.connectchat.presence.repository;

import com.connectchat.presence.entity.PresenceSession;
import com.connectchat.presence.entity.PresenceStatus;
import java.util.List;
import java.util.UUID;

public interface PresenceRepository {
    void saveSession(PresenceSession session);

    void removeSession(String sessionId);

    PresenceStatus getPresence(UUID userId);

    List<PresenceStatus> getPresence(List<UUID> userIds);

    boolean isUserOnline(UUID userId);
}
