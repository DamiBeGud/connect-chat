package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.response.PrivateMessageResponse;
import com.connectchat.chat.common.messaging.PrivateMessageCommand;
import com.connectchat.chat.service.MessageDeliveryService;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageDeliveryServiceImpl implements MessageDeliveryService {

    public static final String PRIVATE_MESSAGES_DESTINATION =
        "/queue/private-messages";

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;
    private final Clock clock;

    @Override
    public void deliver(PrivateMessageCommand command) {
        PrivateMessageResponse message = new PrivateMessageResponse(
            UUID.randomUUID(),
            command.senderId(),
            command.recipientId(),
            command.content(),
            Instant.now(clock)
        );

        logUserRegistryState("sender", command.senderId().toString());
        logUserRegistryState("recipient", command.recipientId().toString());

        messagingTemplate.convertAndSendToUser(
            command.recipientId().toString(),
            PRIVATE_MESSAGES_DESTINATION,
            message
        );
        messagingTemplate.convertAndSendToUser(
            command.senderId().toString(),
            PRIVATE_MESSAGES_DESTINATION,
            message
        );

        log.info(
            "Delivered private chat message messageId={} senderId={} recipientId={}",
            message.messageId(),
            message.senderId(),
            message.recipientId()
        );
    }

    private void logUserRegistryState(String label, String userId) {
        SimpUser user = userRegistry.getUser(userId);

        if (user == null) {
            log.info(
                "SimpUserRegistry {} userId={} present=false totalUsers={}",
                label,
                userId,
                userRegistry.getUserCount()
            );
            return;
        }

        log.info(
            "SimpUserRegistry {} userId={} present=true sessionCount={}",
            label,
            user.getName(),
            user.getSessions().size()
        );

        for (SimpSession session : user.getSessions()) {
            log.info(
                "SimpUserRegistry {} userId={} sessionId={} subscriptionCount={}",
                label,
                user.getName(),
                session.getId(),
                session.getSubscriptions().size()
            );

            for (SimpSubscription subscription : session.getSubscriptions()) {
                log.info(
                    "SimpUserRegistry {} userId={} sessionId={} subscriptionId={} destination={}",
                    label,
                    user.getName(),
                    session.getId(),
                    subscription.getId(),
                    subscription.getDestination()
                );
            }
        }
    }
}
