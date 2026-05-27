package com.connectchat.chat.common.messaging;

import java.util.UUID;

public record AiPrivateReplyCommand(
    UUID senderId,
    UUID recipientId,
    String content
) {}
