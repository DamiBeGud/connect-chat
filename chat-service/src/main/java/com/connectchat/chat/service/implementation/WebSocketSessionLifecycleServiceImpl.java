package com.connectchat.chat.service.implementation;

import com.connectchat.chat.client.PresenceClient;
import com.connectchat.chat.config.ChatInstanceInfo;
import com.connectchat.chat.service.LocalSessionRegistry;
import com.connectchat.chat.service.WebSocketSessionLifecycleService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketSessionLifecycleServiceImpl
    implements WebSocketSessionLifecycleService {

    private final LocalSessionRegistry localSessionRegistry;
    private final PresenceClient presenceClient;
    private final ChatInstanceInfo chatInstanceInfo;

    @Override
    public void registerSession(UUID userId, String sessionId) {
        localSessionRegistry.addSession(userId, sessionId);

        try {
            presenceClient.registerSession(
                userId,
                sessionId,
                chatInstanceInfo.getInstanceId()
            );
        } catch (RuntimeException exception) {
            log.warn(
                "Failed to register presence session userId={} sessionId={} instanceId={}",
                userId,
                sessionId,
                chatInstanceInfo.getInstanceId(),
                exception
            );
        }
    }

    @Override
    public void removeSession(String sessionId) {
        localSessionRegistry.removeSession(sessionId);

        try {
            presenceClient.removeSession(sessionId);
        } catch (RuntimeException exception) {
            log.warn(
                "Failed to remove presence session sessionId={}",
                sessionId,
                exception
            );
        }
    }
}
