package com.connectchat.chat.repository;

import com.connectchat.chat.entity.MessageStatusInboxEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageStatusInboxEventRepository
    extends JpaRepository<MessageStatusInboxEvent, UUID> {
    boolean existsBySourceEventId(UUID sourceEventId);

    @Query(
        value = """
            SELECT *
            FROM message_status_inbox_events
            WHERE status = 'PENDING'
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<MessageStatusInboxEvent> findBatchForProcessing(
        @Param("batchSize") int batchSize
    );
}
