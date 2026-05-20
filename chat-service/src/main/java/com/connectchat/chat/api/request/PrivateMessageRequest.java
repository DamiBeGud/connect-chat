package com.connectchat.chat.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PrivateMessageRequest(
    @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String recipientPhoneNumber,
    @NotBlank @Size(max = 4_000) String content
) {}
