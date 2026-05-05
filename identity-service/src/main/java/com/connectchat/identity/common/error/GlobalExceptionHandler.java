package com.connectchat.identity.common.error;

import com.connectchat.identity.common.web.ErrorInfo;
import com.connectchat.identity.common.web.Metadata;
import com.connectchat.identity.common.web.Response;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Response<Void>> handleResourceNotFoundException(
        ResourceNotFoundException exception
    ) {
        return buildErrorResponse(
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND",
            exception.getMessage()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Response<Void>> handleBadRequestException(
        BadRequestException exception
    ) {
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "BAD_REQUEST",
            exception.getMessage()
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Response<Void>> handleForbiddenException(
        ForbiddenException exception
    ) {
        return buildErrorResponse(
            HttpStatus.FORBIDDEN,
            "FORBIDDEN",
            exception.getMessage()
        );
    }

    private ResponseEntity<Response<Void>> buildErrorResponse(
        HttpStatus status,
        String errorCode,
        String message
    ) {
        Response<Void> response = new Response<>(
            new Metadata(
                RequestContextHolder.currentRequestAttributes().getSessionId(),
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase()
            ),
            null,
            new ErrorInfo(errorCode, message)
        );

        return ResponseEntity.status(status).body(response);
    }
}
