package com.connectchat.group.api.response;

import com.connectchat.group.entity.GroupMemberRole;
import java.time.Instant;
import java.util.UUID;

public record GroupMemberResponse(
    UUID groupId,
    UUID userId,
    String displayName,
    GroupMemberRole role,
    Instant joinedAt
) {}
