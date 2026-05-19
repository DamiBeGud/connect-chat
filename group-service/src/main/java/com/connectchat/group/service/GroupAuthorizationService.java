package com.connectchat.group.service;

import com.connectchat.group.common.security.AuthenticatedCaller;
import java.util.UUID;

public interface GroupAuthorizationService {
    void requireUserToken(AuthenticatedCaller caller);

    void requireCanManageGroup(AuthenticatedCaller caller, UUID groupId);

    void requireCanViewMembers(AuthenticatedCaller caller, UUID groupId);

    boolean canSendToGroup(UUID userId, UUID groupId);
}
