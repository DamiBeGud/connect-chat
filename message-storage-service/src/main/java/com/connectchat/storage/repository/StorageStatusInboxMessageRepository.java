package com.connectchat.storage.repository;

import com.connectchat.storage.entity.StorageStatusInboxMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StorageStatusInboxMessageRepository
    extends JpaRepository<StorageStatusInboxMessage, UUID> {
    boolean existsBySourceEventId(UUID sourceEventId);

    @Query(
        value = """
            SELECT *
            FROM storage_status_inbox_messages
            WHERE status = 'PENDING'
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<StorageStatusInboxMessage> findBatchForProcessing(
        @Param("batchSize") int batchSize
    );
}
