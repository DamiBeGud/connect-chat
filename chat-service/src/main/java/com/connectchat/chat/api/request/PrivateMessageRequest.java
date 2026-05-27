package com.connectchat.chat.api.request;

import com.connectchat.chat.common.MessageContentLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PrivateMessageRequest(
    @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String recipientPhoneNumber,
    @NotBlank @Size(max = MessageContentLimits.MAX_LENGTH) String content
) {}
