package com.connectchat.chat.api.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GroupMessageStatusRequest(@NotNull UUID messageId) {}
