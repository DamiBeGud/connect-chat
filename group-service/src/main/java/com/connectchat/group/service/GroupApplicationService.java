package com.connectchat.group.service;

import com.connectchat.group.api.request.AddGroupMemberRequest;
import com.connectchat.group.api.request.CreateGroupRequest;
import com.connectchat.group.api.response.GroupMemberResponse;
import com.connectchat.group.api.response.GroupResponse;
import com.connectchat.group.common.security.AuthenticatedCaller;
import java.util.List;
import java.util.UUID;

public interface GroupApplicationService {
    GroupResponse createGroup(
        AuthenticatedCaller caller,
        CreateGroupRequest request
    );

    GroupMemberResponse addMember(
        AuthenticatedCaller caller,
        UUID groupId,
        AddGroupMemberRequest request
    );

    void removeMember(
        AuthenticatedCaller caller,
        UUID groupId,
        UUID userId
    );

    void leaveGroup(AuthenticatedCaller caller, UUID groupId);

    List<GroupMemberResponse> getGroupMembers(
        AuthenticatedCaller caller,
        UUID groupId
    );

    List<UUID> getGroupMemberIds(AuthenticatedCaller caller, UUID groupId);

    boolean isMember(UUID groupId, UUID userId);
}
