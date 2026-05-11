package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.service.ChatApplicationService;
import com.connectchat.chat.service.OutboxService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatApplicationServiceImpl implements ChatApplicationService {

    private final OutboxService outboxService;

    @Override
    public void handlePrivateMessage(
        UUID senderId,
        PrivateMessageRequest request
    ) {
        outboxService.enqueuePrivateMessage(senderId, request);
    }
}
