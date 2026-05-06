package com.connectchat.chat.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class WebSocketAuthenticationInterceptorTest {

    private final JwtDecoder jwtDecoder = org.mockito.Mockito.mock(
        JwtDecoder.class
    );
    private final WebSocketAuthenticationInterceptor interceptor =
        new WebSocketAuthenticationInterceptor(jwtDecoder);

    @Test
    void authenticatesConnectFrameFromBearerToken() {
        String userId = "7efe0aac-48b1-4f82-b5cc-85a9fa9ff7cf";
        StompHeaderAccessor accessor = StompHeaderAccessor.create(
            StompCommand.CONNECT
        );
        accessor.setNativeHeader("Authorization", "Bearer token-value");
        Message<byte[]> message = MessageBuilder.createMessage(
            new byte[0],
            accessor.getMessageHeaders()
        );
        when(jwtDecoder.decode("token-value")).thenReturn(jwt(userId));

        Message<?> result = interceptor.preSend(message, null);

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNotNull();
        assertThat(resultAccessor.getUser().getName()).isEqualTo(userId);
    }

    @Test
    void rejectsConnectFrameWithoutBearerToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(
            StompCommand.CONNECT
        );
        Message<byte[]> message = MessageBuilder.createMessage(
            new byte[0],
            accessor.getMessageHeaders()
        );

        assertThatThrownBy(() -> interceptor.preSend(message, null))
            .isInstanceOf(AccessDeniedException.class);
    }

    private Jwt jwt(String subject) {
        return new Jwt(
            "token-value",
            Instant.parse("2026-05-06T10:15:30Z"),
            Instant.parse("2026-05-06T10:30:30Z"),
            Map.of("alg", "HS256"),
            Map.of("sub", subject)
        );
    }
}
