package com.connectchat.chat.client;

import java.util.List;
import java.util.UUID;

public interface GroupClient {
    List<UUID> getGroupMemberIds(UUID groupId);
}
