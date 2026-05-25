package com.connectchat.chat.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record GroupMessageRequest(
    @NotNull UUID groupId,
    @NotBlank @Size(max = 4_000) String content
) {}
