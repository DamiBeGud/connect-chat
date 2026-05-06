package com.connectchat.chat.config;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtDecoder jwtDecoder;
    private final Map<String, Principal> authenticatedSessions =
        new ConcurrentHashMap<>();

    public WebSocketAuthenticationInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

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
                Jwt jwt = jwtDecoder.decode(resolveBearerToken(accessor));
                Principal user = new ChatPrincipal(jwt.getSubject());
                accessor.setUser(user);
                authenticatedSessions.put(resolveSessionId(accessor), user);
                log.info(
                    "Authenticated WebSocket session sessionId={} userId={}",
                    accessor.getSessionId(),
                    user.getName()
                );
            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                authenticatedSessions.remove(accessor.getSessionId());
                log.info(
                    "Disconnected WebSocket session sessionId={}",
                    accessor.getSessionId()
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

        return MessageBuilder.createMessage(
            message.getPayload(),
            accessor.getMessageHeaders()
        );
    }

    private String resolveBearerToken(StompHeaderAccessor accessor) {
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

        return authorization.substring(BEARER_PREFIX.length());
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
