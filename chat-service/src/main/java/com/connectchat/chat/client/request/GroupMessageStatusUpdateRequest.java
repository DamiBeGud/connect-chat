package com.connectchat.chat.client.request;

import com.connectchat.chat.common.messaging.PrivateMessageStatus;

public record GroupMessageStatusUpdateRequest(PrivateMessageStatus status) {}
