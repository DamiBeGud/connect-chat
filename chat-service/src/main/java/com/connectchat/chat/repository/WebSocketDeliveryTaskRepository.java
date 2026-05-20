package com.connectchat.chat.repository;

import com.connectchat.chat.entity.WebSocketDeliveryTask;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WebSocketDeliveryTaskRepository
    extends JpaRepository<WebSocketDeliveryTask, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO websocket_delivery_tasks (
                id,
                source_event_id,
                type,
                target_user_id,
                target_session_id,
                target_instance_id,
                destination,
                payload,
                status,
                expires_at
            )
            VALUES (
                :id,
                :sourceEventId,
                :type,
                :targetUserId,
                :targetSessionId,
                :targetInstanceId,
                :destination,
                :payload,
                'PENDING',
                :expiresAt
            )
            ON CONFLICT (source_event_id, type, target_session_id, destination)
            DO NOTHING
            """,
        nativeQuery = true
    )
    int insertIfAbsent(
        @Param("id") UUID id,
        @Param("sourceEventId") UUID sourceEventId,
        @Param("type") String type,
        @Param("targetUserId") UUID targetUserId,
        @Param("targetSessionId") String targetSessionId,
        @Param("targetInstanceId") String targetInstanceId,
        @Param("destination") String destination,
        @Param("payload") String payload,
        @Param("expiresAt") Instant expiresAt
    );

    @Query(
        value = """
            SELECT *
            FROM websocket_delivery_tasks
            WHERE target_instance_id = :instanceId
              AND status = 'PENDING'
              AND expires_at > NOW()
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<WebSocketDeliveryTask> findBatchForProcessing(
        @Param("instanceId") String instanceId,
        @Param("batchSize") int batchSize
    );

    @Modifying
    @Query(
        value = """
            UPDATE websocket_delivery_tasks
            SET status = 'EXPIRED',
                locked_at = NULL,
                error_message = 'Task expired',
                updated_at = NOW()
            WHERE status IN ('PENDING', 'FAILED', 'PROCESSING')
              AND expires_at <= NOW()
            """,
        nativeQuery = true
    )
    int expireDueTasks();

    @Modifying
    @Query(
        value = """
            UPDATE websocket_delivery_tasks
            SET status = 'PENDING',
                locked_at = NULL,
                error_message = 'Reset after stale processing lock',
                updated_at = NOW()
            WHERE status = 'PROCESSING'
              AND locked_at < :lockedBefore
              AND expires_at > NOW()
            """,
        nativeQuery = true
    )
    int resetStaleProcessingTasks(@Param("lockedBefore") Instant lockedBefore);
}
