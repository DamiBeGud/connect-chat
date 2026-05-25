package com.connectchat.chat.repository;

import com.connectchat.chat.entity.GroupOutboxMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupOutboxMessageRepository
    extends JpaRepository<GroupOutboxMessage, UUID> {
    @Query(
        value = """
            SELECT *
            FROM group_outbox_messages
            WHERE status = 'PENDING'
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<GroupOutboxMessage> findBatchForProcessing(
        @Param("batchSize") int batchSize
    );
}
