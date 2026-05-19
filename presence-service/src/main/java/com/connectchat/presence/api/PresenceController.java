package com.connectchat.presence.api;

import com.connectchat.presence.api.request.PresenceLookupRequest;
import com.connectchat.presence.api.request.RegisterPresenceSessionRequest;
import com.connectchat.presence.api.response.PresenceResponse;
import com.connectchat.presence.api.response.UserOnlineResponse;
import com.connectchat.presence.common.web.Response;
import com.connectchat.presence.common.web.ResponseFactory;
import com.connectchat.presence.service.PresenceApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceApplicationService presenceApplicationService;
    private final ResponseFactory responseFactory;

    @PostMapping("/sessions")
    public ResponseEntity<Response<Void>> registerSession(
        @Valid @RequestBody RegisterPresenceSessionRequest request
    ) {
        presenceApplicationService.registerSession(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                responseFactory.success(
                    HttpStatus.CREATED,
                    "Presence session registered",
                    null
                )
            );
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Response<Void>> removeSession(
        @PathVariable String sessionId
    ) {
        presenceApplicationService.removeSession(sessionId);

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "Presence session removed",
                null
            )
        );
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Response<PresenceResponse>> getPresence(
        @PathVariable UUID userId
    ) {
        PresenceResponse presence =
            presenceApplicationService.getUserPresence(userId);

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "Presence fetched",
                presence
            )
        );
    }

    @GetMapping("/users/{userId}/online")
    public ResponseEntity<Response<UserOnlineResponse>> isOnline(
        @PathVariable UUID userId
    ) {
        UserOnlineResponse online = new UserOnlineResponse(
            userId,
            presenceApplicationService.isUserOnline(userId)
        );

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "Presence status fetched",
                online
            )
        );
    }

    @PostMapping("/users/lookup")
    public ResponseEntity<Response<List<PresenceResponse>>> lookupPresence(
        @Valid @RequestBody PresenceLookupRequest request
    ) {
        List<PresenceResponse> presence =
            presenceApplicationService.getUsersPresence(request.userIds());

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "Presence lookup fetched",
                presence
            )
        );
    }
}
