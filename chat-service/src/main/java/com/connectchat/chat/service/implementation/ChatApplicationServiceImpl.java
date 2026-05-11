package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.common.messaging.PrivateMessagePublisher;
import com.connectchat.chat.service.ChatApplicationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatApplicationServiceImpl implements ChatApplicationService {

    private final PrivateMessagePublisher privateMessagePublisher;

    @Override
    public void handlePrivateMessage(
        UUID senderId,
        PrivateMessageRequest request
    ) {
        privateMessagePublisher.publish(senderId, request);
    }
}
