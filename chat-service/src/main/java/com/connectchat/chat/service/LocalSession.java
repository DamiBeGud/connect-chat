package com.connectchat.chat.service;

import java.time.Instant;
import java.util.UUID;

public record LocalSession(UUID userId, String sessionId, Instant connectedAt) {}
