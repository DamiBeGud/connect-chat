package com.connectchat.chat.client.response;

import java.util.UUID;

public record UserOnlineResponse(UUID userId, boolean online) {}
