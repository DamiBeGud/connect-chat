package com.connectchat.chat.client.request;

import java.util.List;
import java.util.UUID;

public record PresenceLookupRequest(List<UUID> userIds) {}
