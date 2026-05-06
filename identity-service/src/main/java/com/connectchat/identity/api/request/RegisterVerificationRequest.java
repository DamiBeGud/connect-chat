package com.connectchat.identity.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterVerificationRequest(
    @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String phoneNumber,

    @NotBlank @Pattern(regexp = "^\\d{6}$") String verificationCode
) {}
