package com.connectchat.group.api;

import com.connectchat.group.api.request.AddGroupMemberRequest;
import com.connectchat.group.api.request.CreateGroupRequest;
import com.connectchat.group.api.response.GroupMemberResponse;
import com.connectchat.group.api.response.GroupResponse;
import com.connectchat.group.common.security.AuthenticatedCaller;
import com.connectchat.group.common.web.Response;
import com.connectchat.group.common.web.ResponseFactory;
import com.connectchat.group.service.GroupApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupApplicationService groupApplicationService;
    private final ResponseFactory responseFactory;

    @PostMapping
    public ResponseEntity<Response<GroupResponse>> createGroup(
        @AuthenticationPrincipal AuthenticatedCaller caller,
        @Valid @RequestBody CreateGroupRequest request
    ) {
        GroupResponse group = groupApplicationService.createGroup(
            caller,
            request
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                responseFactory.success(
                    HttpStatus.CREATED,
                    "Group created",
                    group
                )
            );
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<Response<GroupMemberResponse>> addMember(
        @AuthenticationPrincipal AuthenticatedCaller caller,
        @PathVariable UUID groupId,
        @Valid @RequestBody AddGroupMemberRequest request
    ) {
        GroupMemberResponse member = groupApplicationService.addMember(
            caller,
            groupId,
            request
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                responseFactory.success(
                    HttpStatus.CREATED,
                    "Group member added",
                    member
                )
            );
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Response<Void>> removeMember(
        @AuthenticationPrincipal AuthenticatedCaller caller,
        @PathVariable UUID groupId,
        @PathVariable UUID userId
    ) {
        groupApplicationService.removeMember(caller, groupId, userId);

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "Group member removed",
                null
            )
        );
    }

    @DeleteMapping("/{groupId}/members/me")
    public ResponseEntity<Response<Void>> leaveGroup(
        @AuthenticationPrincipal AuthenticatedCaller caller,
        @PathVariable UUID groupId
    ) {
        groupApplicationService.leaveGroup(caller, groupId);

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "Left group",
                null
            )
        );
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<Response<List<GroupMemberResponse>>> getMembers(
        @AuthenticationPrincipal AuthenticatedCaller caller,
        @PathVariable UUID groupId
    ) {
        List<GroupMemberResponse> members =
            groupApplicationService.getGroupMembers(caller, groupId);

        return ResponseEntity.ok(
            responseFactory.success(
                HttpStatus.OK,
                "Group members fetched",
                members
            )
        );
    }
}
