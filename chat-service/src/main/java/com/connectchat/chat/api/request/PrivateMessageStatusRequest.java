package com.connectchat.chat.api.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PrivateMessageStatusRequest(@NotNull UUID messageId) {}
