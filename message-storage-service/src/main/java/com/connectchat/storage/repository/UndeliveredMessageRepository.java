package com.connectchat.storage.repository;

import com.connectchat.storage.entity.UndeliveredMessage;
import com.connectchat.storage.entity.UndeliveredMessageKey;
import java.util.List;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UndeliveredMessageRepository
    extends CassandraRepository<UndeliveredMessage, UndeliveredMessageKey> {
    @Query(
        "SELECT * FROM undelivered_messages_by_recipient WHERE recipient_id = :recipientId LIMIT :limit"
    )
    List<UndeliveredMessage> findByRecipientId(
        @Param("recipientId") UUID recipientId,
        @Param("limit") int limit
    );
}
