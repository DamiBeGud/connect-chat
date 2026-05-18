package com.connectchat.chat.service.implementation;

import com.connectchat.chat.common.messaging.RabbitPrivateMessagePublisher;
import com.connectchat.chat.common.messaging.RabbitMessageStatusRequestPublisher;
import com.connectchat.chat.common.messaging.config.ChatMessagingProperties;
import com.connectchat.chat.common.messaging.PrivateMessageCommand;
import com.connectchat.chat.entity.InboxMessage;
import com.connectchat.chat.entity.MessageStatusInboxEvent;
import com.connectchat.chat.entity.MessageStatusOutboxEvent;
import com.connectchat.chat.entity.OutboxMessage;
import com.connectchat.chat.service.InboxService;
import com.connectchat.chat.service.MessageDeliveryService;
import com.connectchat.chat.service.MessageStatusInboxService;
import com.connectchat.chat.service.MessageStatusNotificationService;
import com.connectchat.chat.service.MessageStatusOutboxService;
import com.connectchat.chat.service.OutboxService;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
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
    private final MessageStatusOutboxService messageStatusOutboxService;
    private final RabbitMessageStatusRequestPublisher rabbitMessageStatusRequestPublisher;
    private final MessageStatusInboxService messageStatusInboxService;
    private final MessageStatusNotificationService messageStatusNotificationService;
    private final ChatMessagingProperties properties;

    @Scheduled(fixedDelayString = "${chat.messaging.outbox-processing-delay:1000}")
    public void processOutboxMessages() {
        processBatch(
            outboxService.claimNextBatch(properties.outboxBatchSize()),
            message -> rabbitPrivateMessagePublisher.publish(message.toEvent()),
            message -> outboxService.markProcessed(message.getId()),
            (message, exception) ->
                outboxService.markFailed(message.getId(), exception.getMessage()),
            OutboxMessage::getId,
            "Failed to publish outbox message id={}"
        );
    }

    @Scheduled(fixedDelayString = "${chat.messaging.inbox-processing-delay:1000}")
    public void processInboxMessages() {
        processBatch(
            inboxService.claimNextBatch(properties.inboxBatchSize()),
            message ->
                messageDeliveryService.deliver(
                    new PrivateMessageCommand(
                        message.getSourceMessageId(),
                        message.getSenderId(),
                        message.getRecipientId(),
                        message.getContent(),
                        message.getOccurredAt()
                    )
                ),
            message -> inboxService.markProcessed(message.getId()),
            (message, exception) ->
                inboxService.markFailed(message.getId(), exception.getMessage()),
            InboxMessage::getId,
            "Failed to deliver inbox message id={}"
        );
    }

    @Scheduled(fixedDelayString = "${chat.messaging.outbox-processing-delay:1000}")
    public void processStatusOutboxEvents() {
        processBatch(
            messageStatusOutboxService.claimNextBatch(properties.outboxBatchSize()),
            event -> rabbitMessageStatusRequestPublisher.publish(event.toEvent()),
            event -> messageStatusOutboxService.markProcessed(event.getId()),
            (event, exception) ->
                messageStatusOutboxService.markFailed(
                    event.getId(),
                    exception.getMessage()
                ),
            MessageStatusOutboxEvent::getId,
            "Failed to publish message status outbox event id={}"
        );
    }

    @Scheduled(fixedDelayString = "${chat.messaging.inbox-processing-delay:1000}")
    public void processStatusInboxEvents() {
        processBatch(
            messageStatusInboxService.claimNextBatch(properties.inboxBatchSize()),
            messageStatusNotificationService::notifyUsers,
            event -> messageStatusInboxService.markProcessed(event.getId()),
            (event, exception) ->
                messageStatusInboxService.markFailed(
                    event.getId(),
                    exception.getMessage()
                ),
            MessageStatusInboxEvent::getId,
            "Failed to notify users about message status event id={}"
        );
    }

    private <T> void processBatch(
        List<T> items,
        Consumer<T> handler,
        Consumer<T> onSuccess,
        BiConsumer<T, RuntimeException> onFailure,
        Function<T, UUID> idExtractor,
        String logMessage
    ) {
        for (T item : items) {
            try {
                handler.accept(item);
                onSuccess.accept(item);
            } catch (RuntimeException exception) {
                onFailure.accept(item, exception);
                log.warn(logMessage, idExtractor.apply(item), exception);
            }
        }
    }
}
