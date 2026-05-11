package com.connectchat.storage.repository;

import com.connectchat.storage.entity.StorageInboxMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StorageInboxMessageRepository
    extends JpaRepository<StorageInboxMessage, UUID> {
    boolean existsBySourceMessageId(UUID sourceMessageId);

    @Query(
        value = """
            SELECT *
            FROM storage_inbox_messages
            WHERE status = 'PENDING'
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<StorageInboxMessage> findBatchForProcessing(
        @Param("batchSize") int batchSize
    );
}
