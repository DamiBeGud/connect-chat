package com.connectchat.chat.repository;

import com.connectchat.chat.entity.MessageStatusOutboxEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageStatusOutboxEventRepository
    extends JpaRepository<MessageStatusOutboxEvent, UUID> {
    @Query(
        value = """
            SELECT *
            FROM message_status_outbox_events
            WHERE status = 'PENDING'
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<MessageStatusOutboxEvent> findBatchForProcessing(
        @Param("batchSize") int batchSize
    );
}
