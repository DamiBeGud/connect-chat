package com.connectchat.storage.entity;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("messages_by_id")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @PersistenceCreator)
public class StoredMessage {

    @PrimaryKeyColumn(
        name = "message_id",
        ordinal = 0,
        type = PrimaryKeyType.PARTITIONED
    )
    private UUID messageId;

    @Column("source_inbox_message_id")
    private UUID sourceInboxMessageId;

    @Column("sender_id")
    private UUID senderId;

    @Column("recipient_id")
    private UUID recipientId;

    @Column
    private String content;

    @Column
    @CassandraType(type = CassandraType.Name.TEXT)
    private String status;

    @Column("sent_at")
    private Instant sentAt;

    @Column("updated_at")
    private Instant updatedAt;

    public static StoredMessage fromInbox(StorageInboxMessage inboxMessage) {
        Instant now = Instant.now();
        return StoredMessage.builder()
            .messageId(
                inboxMessage.getSourceMessageId() != null
                    ? inboxMessage.getSourceMessageId()
                    : inboxMessage.getId()
            )
            .sourceInboxMessageId(inboxMessage.getId())
            .senderId(inboxMessage.getSenderId())
            .recipientId(inboxMessage.getRecipientId())
            .content(inboxMessage.getContent())
            .status(StoredMessageStatus.SENT.name())
            .sentAt(
                inboxMessage.getEventOccurredAt() != null
                    ? inboxMessage.getEventOccurredAt()
                    : now
            )
            .updatedAt(now)
            .build();
    }

    public void updateStatus(StoredMessageStatus newStatus) {
        StoredMessageStatus currentStatus = StoredMessageStatus.valueOf(status);
        if (newStatus.ordinal() < currentStatus.ordinal()) {
            return;
        }
        status = newStatus.name();
        updatedAt = Instant.now();
    }
}
