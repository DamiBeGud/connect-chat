package com.connectchat.chat.repository;

import com.connectchat.chat.entity.InboxMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InboxMessageRepository extends JpaRepository<InboxMessage, UUID> {
    boolean existsBySourceOutboxMessageId(UUID sourceOutboxMessageId);

    @Query(
        value = """
            SELECT *
            FROM inbox_messages
            WHERE status = 'PENDING'
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<InboxMessage> findBatchForProcessing(@Param("batchSize") int batchSize);
}
