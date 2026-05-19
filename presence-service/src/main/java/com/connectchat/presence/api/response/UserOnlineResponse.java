package com.connectchat.presence.api.response;

import java.util.UUID;

public record UserOnlineResponse(UUID userId, boolean online) {}
