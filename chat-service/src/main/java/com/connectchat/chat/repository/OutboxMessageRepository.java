package com.connectchat.chat.repository;

import com.connectchat.chat.entity.OutboxMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {
    @Query(
        value = """
            SELECT *
            FROM outbox_messages
            WHERE status = 'PENDING'
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<OutboxMessage> findBatchForProcessing(@Param("batchSize") int batchSize);
}
