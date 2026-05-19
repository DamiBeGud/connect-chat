package com.connectchat.identity.api.request;

import jakarta.validation.constraints.NotBlank;

public record ServiceTokenRequest(
    @NotBlank String clientId,
    @NotBlank String clientSecret
) {}
