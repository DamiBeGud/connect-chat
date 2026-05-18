package com.connectchat.chat.service.implementation;

import com.connectchat.chat.common.messaging.RabbitPrivateMessagePublisher;
import com.connectchat.chat.common.messaging.config.ChatMessagingProperties;
import com.connectchat.chat.common.messaging.PrivateMessageCommand;
import com.connectchat.chat.entity.InboxMessage;
import com.connectchat.chat.entity.OutboxMessage;
import com.connectchat.chat.service.InboxService;
import com.connectchat.chat.service.MessageDeliveryService;
import com.connectchat.chat.service.OutboxService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessagePipelineService {

    private final OutboxService outboxService;
    private final RabbitPrivateMessagePublisher rabbitPrivateMessagePublisher;
    private final InboxService inboxService;
    private final MessageDeliveryService messageDeliveryService;
    private final ChatMessagingProperties properties;

    @Scheduled(fixedDelayString = "${chat.messaging.outbox-processing-delay:1000}")
    public void processOutboxMessages() {
        List<OutboxMessage> messages = outboxService.claimNextBatch(
            properties.outboxBatchSize()
        );
        for (OutboxMessage message : messages) {
            try {
                rabbitPrivateMessagePublisher.publish(message.toEvent());
                outboxService.markProcessed(message.getId());
            } catch (RuntimeException exception) {
                outboxService.markFailed(message.getId(), exception.getMessage());
                log.warn(
                    "Failed to publish outbox message id={} to RabbitMQ",
                    message.getId(),
                    exception
                );
            }
        }
    }

    @Scheduled(fixedDelayString = "${chat.messaging.inbox-processing-delay:1000}")
    public void processInboxMessages() {
        List<InboxMessage> messages = inboxService.claimNextBatch(
            properties.inboxBatchSize()
        );
        for (InboxMessage message : messages) {
            try {
                messageDeliveryService.deliver(
                    new PrivateMessageCommand(
                        message.getSourceMessageId(),
                        message.getSenderId(),
                        message.getRecipientId(),
                        message.getContent(),
                        message.getOccurredAt()
                    )
                );
                inboxService.markProcessed(message.getId());
            } catch (RuntimeException exception) {
                inboxService.markFailed(message.getId(), exception.getMessage());
                log.warn(
                    "Failed to deliver inbox message id={}",
                    message.getId(),
                    exception
                );
            }
        }
    }
}
