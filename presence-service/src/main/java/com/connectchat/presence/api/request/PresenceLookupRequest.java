package com.connectchat.presence.api.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record PresenceLookupRequest(@NotEmpty List<UUID> userIds) {}
