package com.connectchat.storage.repository;

import com.connectchat.storage.entity.UndeliveredGroupMessage;
import com.connectchat.storage.entity.UndeliveredGroupMessageKey;
import java.util.List;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UndeliveredGroupMessageRepository
    extends CassandraRepository<UndeliveredGroupMessage, UndeliveredGroupMessageKey> {
    @Query(
        "SELECT * FROM undelivered_group_messages_by_recipient WHERE recipient_id = :recipientId LIMIT :limit"
    )
    List<UndeliveredGroupMessage> findByRecipientId(
        @Param("recipientId") UUID recipientId,
        @Param("limit") int limit
    );
}
