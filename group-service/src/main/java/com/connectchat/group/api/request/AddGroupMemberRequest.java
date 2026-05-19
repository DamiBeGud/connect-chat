package com.connectchat.group.api.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddGroupMemberRequest(@NotNull UUID userId) {}
