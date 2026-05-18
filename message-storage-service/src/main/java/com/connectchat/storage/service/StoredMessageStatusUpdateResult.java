package com.connectchat.storage.service;

import com.connectchat.storage.entity.StoredMessage;

public record StoredMessageStatusUpdateResult(
    StoredMessage storedMessage,
    boolean statusChanged
) {}
