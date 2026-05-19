package com.connectchat.presence.common.web;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class ResponseFactory {

    public <T> Response<T> success(
        HttpStatus status,
        String message,
        T data
    ) {
        return new Response<>(
            new Metadata(
                RequestContextHolder.currentRequestAttributes().getSessionId(),
                Instant.now().toString(),
                status.value(),
                message
            ),
            data,
            null
        );
    }
}
