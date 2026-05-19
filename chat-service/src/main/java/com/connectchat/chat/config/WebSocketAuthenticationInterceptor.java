package com.connectchat.chat.config;

import com.connectchat.chat.client.IdentityAuthClient;
import com.connectchat.chat.client.response.IdentityTokenValidationResponse;
import com.connectchat.chat.service.WebSocketSessionLifecycleService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_TOKEN_TYPE = "user";
    private static final String USER_ROLE = "USER";

    private final IdentityAuthClient identityAuthClient;
    private final WebSocketSessionLifecycleService webSocketSessionLifecycleService;
    private final Map<String, Principal> authenticatedSessions =
        new ConcurrentHashMap<>();

    public WebSocketAuthenticationInterceptor(
        IdentityAuthClient identityAuthClient,
        WebSocketSessionLifecycleService webSocketSessionLifecycleService
    ) {
        this.identityAuthClient = identityAuthClient;
        this.webSocketSessionLifecycleService = webSocketSessionLifecycleService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message,
            StompHeaderAccessor.class
        );

        if (accessor == null) {
            return message;
        }

        try {
            log.debug(
                "Inbound STOMP frame command={} sessionId={} destination={} user={}",
                accessor.getCommand(),
                accessor.getSessionId(),
                accessor.getDestination(),
                accessor.getUser() == null
                    ? null
                    : accessor.getUser().getName()
            );

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                IdentityTokenValidationResponse token =
                    validateUserToken(resolveAuthorizationHeader(accessor));
                UUID userId = UUID.fromString(token.subject());
                Principal user = new ChatPrincipal(userId.toString());
                String sessionId = resolveSessionId(accessor);
                accessor.setUser(user);
                authenticatedSessions.put(sessionId, user);
                webSocketSessionLifecycleService.registerSession(
                    userId,
                    sessionId
                );
                log.info(
                    "Authenticated WebSocket session sessionId={} userId={}",
                    accessor.getSessionId(),
                    user.getName()
                );
            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                String sessionId = resolveSessionId(accessor);
                authenticatedSessions.remove(sessionId);
                webSocketSessionLifecycleService.removeSession(sessionId);
                log.info(
                    "Disconnected WebSocket session sessionId={}",
                    sessionId
                );
            } else if (requiresAuthenticatedUser(accessor)) {
                Principal user = authenticatedSessions.get(
                    resolveSessionId(accessor)
                );

                if (user == null) {
                    throw new AccessDeniedException(
                        "WebSocket user is not authenticated"
                    );
                }

                accessor.setUser(user);

                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    log.info(
                        "WebSocket subscription sessionId={} userId={} subscriptionId={} destination={}",
                        accessor.getSessionId(),
                        user.getName(),
                        accessor.getSubscriptionId(),
                        accessor.getDestination()
                    );
                }
            }
        } catch (RuntimeException exception) {
            log.warn(
                "Rejected STOMP frame command={} sessionId={} destination={} reason={}",
                accessor.getCommand(),
                accessor.getSessionId(),
                accessor.getDestination(),
                exception.getMessage(),
                exception
            );
            throw exception;
        }

        return message;
    }

    private IdentityTokenValidationResponse validateUserToken(
        String authorizationHeader
    ) {
        IdentityTokenValidationResponse token =
            identityAuthClient.validateToken(authorizationHeader);

        if (
            !USER_TOKEN_TYPE.equals(token.tokenType()) ||
            !USER_ROLE.equals(token.role())
        ) {
            throw new AccessDeniedException(
                "WebSocket connection requires a user token"
            );
        }

        try {
            UUID.fromString(token.subject());
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException(
                "WebSocket user token subject must be a valid UUID"
            );
        }

        return token;
    }

    private String resolveAuthorizationHeader(StompHeaderAccessor accessor) {
        List<String> authorizationHeaders =
            accessor.getNativeHeader("Authorization");

        if (authorizationHeaders == null || authorizationHeaders.isEmpty()) {
            throw new AccessDeniedException(
                "Missing Authorization header for WebSocket connection"
            );
        }

        String authorization = authorizationHeaders.getFirst();
        if (
            authorization == null || !authorization.startsWith(BEARER_PREFIX)
        ) {
            throw new AccessDeniedException(
                "Authorization header must use Bearer authentication"
            );
        }

        return authorization;
    }

    private String resolveSessionId(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();

        if (sessionId == null) {
            throw new AccessDeniedException("Missing WebSocket session id");
        }

        return sessionId;
    }

    private boolean requiresAuthenticatedUser(StompHeaderAccessor accessor) {
        return StompCommand.SEND.equals(accessor.getCommand()) ||
        StompCommand.SUBSCRIBE.equals(accessor.getCommand());
    }

    private record ChatPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
