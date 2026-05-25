package com.connectchat.storage.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UndeliveredGroupMessageKey implements Serializable {

    @PrimaryKeyColumn(
        name = "recipient_id",
        ordinal = 0,
        type = PrimaryKeyType.PARTITIONED
    )
    private UUID recipientId;

    @PrimaryKeyColumn(name = "sent_at", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private Instant sentAt;

    @PrimaryKeyColumn(
        name = "message_id",
        ordinal = 2,
        type = PrimaryKeyType.CLUSTERED
    )
    private UUID messageId;
}
