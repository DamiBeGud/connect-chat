package com.connectchat.storage.api.request;

import com.connectchat.storage.entity.StoredMessageStatus;

public record GroupMessageStatusUpdateRequest(StoredMessageStatus status) {}
