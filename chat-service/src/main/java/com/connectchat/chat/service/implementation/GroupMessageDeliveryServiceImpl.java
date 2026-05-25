package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.response.GroupMessageResponse;
import com.connectchat.chat.client.IdentityUserClient;
import com.connectchat.chat.client.response.IdentityUserResponse;
import com.connectchat.chat.common.messaging.GroupMessageCommand;
import com.connectchat.chat.service.GroupMessageDeliveryService;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupMessageDeliveryServiceImpl
    implements GroupMessageDeliveryService {

    private final WebSocketDeliveryFanoutService fanoutService;
    private final IdentityUserClient identityUserClient;
    private final Clock clock;

    @Override
    public void deliver(GroupMessageCommand command) {
        IdentityUserResponse sender = identityUserClient.getUserById(
            command.senderId()
        );
        GroupMessageResponse message = new GroupMessageResponse(
            command.messageId() != null ? command.messageId() : UUID.randomUUID(),
            command.groupId(),
            command.senderId(),
            sender.phoneNumber(),
            command.content(),
            command.occurredAt() != null ? command.occurredAt() : Instant.now(clock)
        );

        fanoutService.createGroupMessageTasks(message, command.recipientIds());
    }
}
