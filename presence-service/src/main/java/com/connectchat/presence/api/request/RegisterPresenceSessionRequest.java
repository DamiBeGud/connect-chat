package com.connectchat.presence.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegisterPresenceSessionRequest(
    @NotNull UUID userId,
    @NotBlank String sessionId,
    @NotBlank String instanceId
) {}
