package com.connectchat.chat.api.request;

import com.connectchat.chat.common.MessageContentLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record GroupMessageRequest(
    @NotNull UUID groupId,
    @NotBlank @Size(max = MessageContentLimits.MAX_LENGTH) String content
) {}
