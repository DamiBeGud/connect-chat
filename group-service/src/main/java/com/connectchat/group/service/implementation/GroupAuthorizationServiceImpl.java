package com.connectchat.group.service.implementation;

import com.connectchat.group.common.error.ForbiddenException;
import com.connectchat.group.common.security.AuthenticatedCaller;
import com.connectchat.group.repository.GroupMemberRepository;
import com.connectchat.group.repository.GroupRepository;
import com.connectchat.group.service.GroupAuthorizationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupAuthorizationServiceImpl
    implements GroupAuthorizationService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Override
    public void requireUserToken(AuthenticatedCaller caller) {
        if (!caller.isUserToken()) {
            throw new ForbiddenException("User token is required");
        }
    }

    @Override
    public void requireCanManageGroup(
        AuthenticatedCaller caller,
        UUID groupId
    ) {
        requireUserToken(caller);

        if (!groupRepository.existsByIdAndOwnerId(groupId, caller.requireUserId())) {
            throw new ForbiddenException("Only the group owner can manage members");
        }
    }

    @Override
    public void requireCanViewMembers(
        AuthenticatedCaller caller,
        UUID groupId
    ) {
        if (caller.isServiceToken()) {
            return;
        }

        requireUserToken(caller);

        if (!groupMemberRepository.existsByGroupIdAndUserId(
            groupId,
            caller.requireUserId()
        )) {
            throw new ForbiddenException("Only group members can view members");
        }
    }

    @Override
    public boolean canSendToGroup(UUID userId, UUID groupId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }
}
