package com.connectchat.chat.common.messaging;

import java.util.UUID;

public record PrivateMessageCommand(
    UUID senderId,
    UUID recipientId,
    String content
) {}
