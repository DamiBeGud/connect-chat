package com.connectchat.chat.service;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import java.util.UUID;

public interface ChatApplicationService {
    void handlePrivateMessage(
        UUID senderId,
        PrivateMessageRequest request
    );
}
