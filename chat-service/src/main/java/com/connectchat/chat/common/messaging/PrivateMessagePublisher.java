package com.connectchat.chat.common.messaging;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import java.util.UUID;

public interface PrivateMessagePublisher {
    void publish(UUID senderId, PrivateMessageRequest request);
}
