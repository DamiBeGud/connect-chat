package com.connectchat.chat.service.implementation;

import com.connectchat.chat.api.request.PrivateMessageRequest;
import com.connectchat.chat.common.messaging.PrivateMessageCommand;
import com.connectchat.chat.common.messaging.PrivateMessagePublisher;
import com.connectchat.chat.service.ChatApplicationService;
import com.connectchat.chat.service.MessageDeliveryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatApplicationServiceImpl implements ChatApplicationService {

    private final PrivateMessagePublisher privateMessagePublisher;
    private final MessageDeliveryService messageDeliveryService;
    private final ApplicationContext applicationContext;
    @Value("${chat.messaging.deliver-directly:false}")
    private boolean deliverDirectly;

    @Override
    public void handlePrivateMessage(
        UUID senderId,
        PrivateMessageRequest request
    ) {
        log.info(
            "ChatApplicationService path senderId={} recipientId={} deliverDirectly={} applicationContextId={} applicationContextIdentity={}",
            senderId,
            request.recipientId(),
            deliverDirectly,
            applicationContext.getId(),
            System.identityHashCode(applicationContext)
        );

        if (deliverDirectly) {
            messageDeliveryService.deliver(
                new PrivateMessageCommand(
                    senderId,
                    request.recipientId(),
                    request.content()
                )
            );
            return;
        }

        privateMessagePublisher.publish(senderId, request);
    }
}
