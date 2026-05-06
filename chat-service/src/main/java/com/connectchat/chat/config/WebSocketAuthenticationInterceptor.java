package com.connectchat.chat.config;

import java.security.Principal;
import java.util.List;
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
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtDecoder jwtDecoder;

    public WebSocketAuthenticationInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Jwt jwt = jwtDecoder.decode(resolveBearerToken(accessor));
            accessor.setUser(new ChatPrincipal(jwt.getSubject()));
        }

        if (requiresAuthenticatedUser(accessor) && accessor.getUser() == null) {
            throw new AccessDeniedException("WebSocket user is not authenticated");
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
