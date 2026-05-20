package com.connectchat.chat.entity;

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
@Table(name = "websocket_delivery_tasks")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WebSocketDeliveryTask {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "source_event_id", nullable = false, updatable = false)
    private UUID sourceEventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32, updatable = false)
    private WebSocketDeliveryTaskType type;

    @Column(name = "target_user_id", nullable = false, updatable = false)
    private UUID targetUserId;

    @Column(
        name = "target_session_id",
        nullable = false,
        length = 255,
        updatable = false
    )
    private String targetSessionId;

    @Column(
        name = "target_instance_id",
        nullable = false,
        length = 255,
        updatable = false
    )
    private String targetInstanceId;

    @Column(nullable = false, length = 255, updatable = false)
    private String destination;

    @Column(nullable = false, columnDefinition = "TEXT", updatable = false)
    private String payload;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WebSocketDeliveryTaskStatus status =
        WebSocketDeliveryTaskStatus.PENDING;

    @Builder.Default
    @Column(nullable = false)
    private int attempts = 0;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void markProcessing() {
        status = WebSocketDeliveryTaskStatus.PROCESSING;
        attempts += 1;
        lockedAt = Instant.now();
        errorMessage = null;
    }

    public void markProcessed() {
        status = WebSocketDeliveryTaskStatus.PROCESSED;
        lockedAt = null;
        processedAt = Instant.now();
        errorMessage = null;
    }

    public void markFailed(String failureReason) {
        status = WebSocketDeliveryTaskStatus.FAILED;
        lockedAt = null;
        errorMessage = failureReason;
    }

    public void markExpired(String reason) {
        status = WebSocketDeliveryTaskStatus.EXPIRED;
        lockedAt = null;
        errorMessage = reason;
    }

    @PrePersist
    void initialize() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
