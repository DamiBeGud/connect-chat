package com.connectchat.presence.service.implementation;

import com.connectchat.presence.api.request.RegisterPresenceSessionRequest;
import com.connectchat.presence.api.response.PresenceResponse;
import com.connectchat.presence.api.response.PresenceSessionResponse;
import com.connectchat.presence.entity.PresenceSession;
import com.connectchat.presence.entity.PresenceStatus;
import com.connectchat.presence.repository.PresenceRepository;
import com.connectchat.presence.service.PresenceApplicationService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PresenceApplicationServiceImpl
    implements PresenceApplicationService {

    private final PresenceRepository presenceRepository;
    private final Clock clock;

    @Override
    public void registerSession(RegisterPresenceSessionRequest request) {
        Instant now = Instant.now(clock);
        presenceRepository.saveSession(
            new PresenceSession(
                request.userId(),
                request.sessionId(),
                request.instanceId(),
                now,
                now
            )
        );
    }

    @Override
    public void removeSession(String sessionId) {
        presenceRepository.removeSession(sessionId);
    }

    @Override
    public PresenceResponse getUserPresence(UUID userId) {
        return toResponse(presenceRepository.getPresence(userId));
    }

    @Override
    public List<PresenceResponse> getUsersPresence(List<UUID> userIds) {
        return presenceRepository
            .getPresence(userIds)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public boolean isUserOnline(UUID userId) {
        return presenceRepository.isUserOnline(userId);
    }

    private PresenceResponse toResponse(PresenceStatus presence) {
        return new PresenceResponse(
            presence.userId(),
            presence.status(),
            presence.sessions().stream().map(this::toSessionResponse).toList(),
            presence.updatedAt()
        );
    }

    private PresenceSessionResponse toSessionResponse(PresenceSession session) {
        return new PresenceSessionResponse(
            session.userId(),
            session.sessionId(),
            session.instanceId(),
            session.connectedAt(),
            session.lastHeartbeatAt()
        );
    }
}
