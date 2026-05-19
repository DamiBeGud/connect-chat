package com.connectchat.chat.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectchat.chat.client.IdentityAuthClient;
import com.connectchat.chat.client.response.IdentityTokenValidationResponse;
import com.connectchat.chat.service.WebSocketSessionLifecycleService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;

class WebSocketAuthenticationInterceptorTest {

    private final IdentityAuthClient identityAuthClient = org.mockito.Mockito.mock(
        IdentityAuthClient.class
    );
    private final WebSocketSessionLifecycleService webSocketSessionLifecycleService =
        org.mockito.Mockito.mock(WebSocketSessionLifecycleService.class);
    private final WebSocketAuthenticationInterceptor interceptor =
        new WebSocketAuthenticationInterceptor(
            identityAuthClient,
            webSocketSessionLifecycleService
        );

    @Test
    void authenticatesConnectFrameFromBearerToken() {
        String userId = "7efe0aac-48b1-4f82-b5cc-85a9fa9ff7cf";
        StompHeaderAccessor accessor = StompHeaderAccessor.create(
            StompCommand.CONNECT
        );
        accessor.setSessionId("session-1");
        accessor.setNativeHeader("Authorization", "Bearer token-value");
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(
            new byte[0],
            accessor.getMessageHeaders()
        );
        when(identityAuthClient.validateToken("Bearer token-value"))
            .thenReturn(userToken(userId));

        Message<?> result = interceptor.preSend(message, null);

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNotNull();
        assertThat(resultAccessor.getUser().getName()).isEqualTo(userId);
        verify(webSocketSessionLifecycleService).registerSession(
            UUID.fromString(userId),
            "session-1"
        );
    }

    @Test
    void attachesAuthenticatedUserToSubscribeFrame() {
        String userId = "7efe0aac-48b1-4f82-b5cc-85a9fa9ff7cf";
        when(identityAuthClient.validateToken("Bearer token-value"))
            .thenReturn(userToken(userId));

        interceptor.preSend(connectMessage("session-1"), null);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(
            StompCommand.SUBSCRIBE
        );
        accessor.setSessionId("session-1");
        accessor.setDestination("/user/queue/private-messages");
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(
            new byte[0],
            accessor.getMessageHeaders()
        );

        Message<?> result = interceptor.preSend(message, null);

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNotNull();
        assertThat(resultAccessor.getUser().getName()).isEqualTo(userId);
    }

    @Test
    void rejectsSubscribeFrameForUnauthenticatedSession() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(
            StompCommand.SUBSCRIBE
        );
        accessor.setSessionId("session-1");
        accessor.setDestination("/user/queue/private-messages");
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(
            new byte[0],
            accessor.getMessageHeaders()
        );

        assertThatThrownBy(() -> interceptor.preSend(message, null))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsConnectFrameWithoutBearerToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(
            StompCommand.CONNECT
        );
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(
            new byte[0],
            accessor.getMessageHeaders()
        );

        assertThatThrownBy(() -> interceptor.preSend(message, null))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsConnectFrameWithServiceToken() {
        when(identityAuthClient.validateToken("Bearer token-value"))
            .thenReturn(
                new IdentityTokenValidationResponse(
                    "chat-service",
                    "service",
                    "INTERNAL_SERVICE",
                    Instant.parse("2026-05-06T10:30:30Z")
                )
            );

        assertThatThrownBy(() -> interceptor.preSend(connectMessage("session-1"), null))
            .isInstanceOf(AccessDeniedException.class);
    }

    private Message<byte[]> connectMessage(String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(
            StompCommand.CONNECT
        );
        accessor.setSessionId(sessionId);
        accessor.setNativeHeader("Authorization", "Bearer token-value");
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(
            new byte[0],
            accessor.getMessageHeaders()
        );
    }

    private IdentityTokenValidationResponse userToken(String subject) {
        return new IdentityTokenValidationResponse(
            subject,
            "user",
            "USER",
            Instant.parse("2026-05-06T10:30:30Z")
        );
    }
}
