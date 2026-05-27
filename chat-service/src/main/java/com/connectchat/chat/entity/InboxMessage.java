package com.connectchat.chat.entity;

import com.connectchat.chat.common.MessageContentLimits;
import com.connectchat.chat.common.messaging.PrivateMessageEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "inbox_messages")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InboxMessage {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(
        name = "source_message_id",
        nullable = false,
        unique = true,
        updatable = false
    )
    private UUID sourceMessageId;

    @Column(name = "sender_id", nullable = false, updatable = false)
    private UUID senderId;

    @Column(name = "recipient_id", nullable = false, updatable = false)
    private UUID recipientId;

    @Column(
        nullable = false,
        length = MessageContentLimits.MAX_LENGTH,
        updatable = false
    )
    private String content;

    @Column(name = "occurred_at", updatable = false)
    private Instant occurredAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MessageProcessingStatus status = MessageProcessingStatus.PENDING;

    @Builder.Default
    @Column(nullable = false)
    private int attempts = 0;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static InboxMessage fromEvent(PrivateMessageEvent event) {
        return InboxMessage.builder()
            .sourceMessageId(event.messageId())
            .senderId(event.senderId())
            .recipientId(event.recipientId())
            .content(event.content())
            .occurredAt(event.occurredAt())
            .build();
    }

    public void markProcessing() {
        status = MessageProcessingStatus.PROCESSING;
        attempts += 1;
        lockedAt = Instant.now();
        errorMessage = null;
    }

    public void markProcessed() {
        status = MessageProcessingStatus.PROCESSED;
        lockedAt = null;
        processedAt = Instant.now();
        errorMessage = null;
    }

    public void markFailed(String failureReason) {
        status = MessageProcessingStatus.FAILED;
        lockedAt = null;
        errorMessage = failureReason;
    }

    @PrePersist
    void initialize() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
