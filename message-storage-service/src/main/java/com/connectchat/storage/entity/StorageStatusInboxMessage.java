package com.connectchat.storage.entity;

import com.connectchat.storage.common.messaging.MessageStatusEvent;
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
@Table(name = "storage_status_inbox_messages")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StorageStatusInboxMessage {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "source_event_id", nullable = false, unique = true, updatable = false)
    private UUID sourceEventId;

    @Column(name = "message_id", nullable = false, updatable = false)
    private UUID messageId;

    @Column(name = "sender_id", nullable = false, updatable = false)
    private UUID senderId;

    @Column(name = "recipient_id", nullable = false, updatable = false)
    private UUID recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_value", nullable = false, length = 32, updatable = false)
    private StoredMessageStatus statusValue;

    @Column(name = "actor_user_id", updatable = false)
    private UUID actorUserId;

    @Column(name = "event_occurred_at", updatable = false)
    private Instant eventOccurredAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MessageInboxStatus status = MessageInboxStatus.PENDING;

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

    public static StorageStatusInboxMessage fromEvent(MessageStatusEvent event) {
        return StorageStatusInboxMessage.builder()
            .sourceEventId(event.eventId())
            .messageId(event.messageId())
            .senderId(event.senderId())
            .recipientId(event.recipientId())
            .statusValue(event.status())
            .actorUserId(event.actorUserId())
            .eventOccurredAt(event.occurredAt())
            .build();
    }

    public void markProcessing() {
        status = MessageInboxStatus.PROCESSING;
        attempts += 1;
        lockedAt = Instant.now();
        errorMessage = null;
    }

    public void markProcessed() {
        status = MessageInboxStatus.PROCESSED;
        lockedAt = null;
        processedAt = Instant.now();
        errorMessage = null;
    }

    public void markFailed(String failureReason) {
        status = MessageInboxStatus.FAILED;
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
