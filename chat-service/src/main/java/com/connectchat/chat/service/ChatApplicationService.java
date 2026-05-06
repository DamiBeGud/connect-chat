package com.connectchat.chat.service;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.api.response.PrivateMessageResponse;
import java.util.UUID;

public interface ChatApplicationService {
    PrivateMessageResponse handlePrivateMessage(
        UUID senderId,
        PrivateMessageRequest request
    );
}
