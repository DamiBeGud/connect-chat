package com.connectchat.group.service.implementation;

import com.connectchat.group.api.request.AddGroupMemberRequest;
import com.connectchat.group.api.request.CreateGroupRequest;
import com.connectchat.group.api.response.GroupMemberResponse;
import com.connectchat.group.api.response.GroupResponse;
import com.connectchat.group.client.IdentityUserClient;
import com.connectchat.group.client.IdentityUserResponse;
import com.connectchat.group.common.error.BadRequestException;
import com.connectchat.group.common.error.ForbiddenException;
import com.connectchat.group.common.error.ResourceNotFoundException;
import com.connectchat.group.common.security.AuthenticatedCaller;
import com.connectchat.group.entity.Group;
import com.connectchat.group.entity.GroupMember;
import com.connectchat.group.entity.GroupMemberRole;
import com.connectchat.group.repository.GroupMemberRepository;
import com.connectchat.group.repository.GroupRepository;
import com.connectchat.group.service.GroupApplicationService;
import com.connectchat.group.service.GroupAuthorizationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupApplicationServiceImpl implements GroupApplicationService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupAuthorizationService groupAuthorizationService;
    private final IdentityUserClient identityUserClient;

    @Override
    @Transactional
    public GroupResponse createGroup(
        AuthenticatedCaller caller,
        CreateGroupRequest request
    ) {
        groupAuthorizationService.requireUserToken(caller);

        UUID ownerId = caller.requireUserId();
        Group group = groupRepository.saveAndFlush(
            Group.builder()
                .ownerId(ownerId)
                .name(request.name())
                .build()
        );

        groupMemberRepository.save(
            GroupMember.builder()
                .groupId(group.getId())
                .userId(ownerId)
                .role(GroupMemberRole.OWNER)
                .build()
        );

        return toGroupResponse(group);
    }

    @Override
    @Transactional
    public GroupMemberResponse addMember(
        AuthenticatedCaller caller,
        UUID groupId,
        AddGroupMemberRequest request
    ) {
        requireGroup(groupId);
        groupAuthorizationService.requireCanManageGroup(caller, groupId);

        IdentityUserResponse user = identityUserClient.getUserByPhoneNumber(
            request.phoneNumber()
        );
        UUID userId = user.userId();

        if (
            groupMemberRepository.existsByGroupIdAndUserId(
                groupId,
                userId
            )
        ) {
            throw new BadRequestException("User is already a group member");
        }

        GroupMember member = groupMemberRepository.saveAndFlush(
            GroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .role(GroupMemberRole.MEMBER)
                .build()
        );

        return toMemberResponse(member);
    }

    @Override
    @Transactional
    public void removeMember(
        AuthenticatedCaller caller,
        UUID groupId,
        UUID userId
    ) {
        Group group = requireGroup(groupId);
        groupAuthorizationService.requireCanManageGroup(caller, groupId);

        deleteMember(group, userId);
    }

    @Override
    @Transactional
    public void leaveGroup(AuthenticatedCaller caller, UUID groupId) {
        groupAuthorizationService.requireUserToken(caller);
        Group group = requireGroup(groupId);

        deleteMember(group, caller.requireUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(
        AuthenticatedCaller caller,
        UUID groupId
    ) {
        requireGroup(groupId);
        groupAuthorizationService.requireCanViewMembers(caller, groupId);

        return groupMemberRepository
            .findByGroupIdOrderByCreatedAtAsc(groupId)
            .stream()
            .map(this::toMemberResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMember(UUID groupId, UUID userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    private void deleteMember(Group group, UUID userId) {
        if (group.getOwnerId().equals(userId)) {
            throw new ForbiddenException("Group owner cannot be removed");
        }

        GroupMember member = groupMemberRepository
            .findByGroupIdAndUserId(group.getId(), userId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Group member was not found")
            );

        groupMemberRepository.delete(member);
    }

    private Group requireGroup(UUID groupId) {
        return groupRepository
            .findById(groupId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Group was not found")
            );
    }

    private GroupResponse toGroupResponse(Group group) {
        return new GroupResponse(
            group.getId(),
            group.getOwnerId(),
            group.getName(),
            group.getCreatedAt()
        );
    }

    private GroupMemberResponse toMemberResponse(GroupMember member) {
        return new GroupMemberResponse(
            member.getGroupId(),
            member.getUserId(),
            member.getRole(),
            member.getCreatedAt()
        );
    }
}
