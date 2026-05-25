package com.connectchat.storage.entity;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("undelivered_messages_by_recipient")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @PersistenceCreator)
public class UndeliveredMessage {

    @PrimaryKey
    private UndeliveredMessageKey key;

    @Column("sender_id")
    private UUID senderId;

    @Column
    private String content;

    @Column
    @CassandraType(type = CassandraType.Name.TEXT)
    private String status;

    @Column("updated_at")
    private Instant updatedAt;

    public static UndeliveredMessage fromStoredMessage(StoredMessage message) {
        return UndeliveredMessage.builder()
            .key(
                new UndeliveredMessageKey(
                    message.getRecipientId(),
                    message.getSentAt(),
                    message.getMessageId()
                )
            )
            .senderId(message.getSenderId())
            .content(message.getContent())
            .status(message.getStatus())
            .updatedAt(message.getUpdatedAt())
            .build();
    }

    public UUID recipientId() {
        return key.getRecipientId();
    }

    public Instant sentAt() {
        return key.getSentAt();
    }

    public UUID messageId() {
        return key.getMessageId();
    }
}
