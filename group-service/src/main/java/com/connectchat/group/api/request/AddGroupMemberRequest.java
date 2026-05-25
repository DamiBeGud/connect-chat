package com.connectchat.group.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddGroupMemberRequest(
    @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String phoneNumber
) {}
