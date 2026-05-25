package com.connectchat.storage.entity;

import com.connectchat.storage.common.messaging.GroupMessageEvent;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("group_messages_by_id")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @PersistenceCreator)
public class GroupStoredMessage {

    @PrimaryKey("message_id")
    private UUID messageId;

    @Column("group_id")
    private UUID groupId;

    @Column("sender_id")
    private UUID senderId;

    @Column
    private String content;

    @Column("sent_at")
    private Instant sentAt;

    @Column("updated_at")
    private Instant updatedAt;

    public static GroupStoredMessage fromEvent(GroupMessageEvent event) {
        Instant now = Instant.now();
        return GroupStoredMessage.builder()
            .messageId(event.messageId())
            .groupId(event.groupId())
            .senderId(event.senderId())
            .content(event.content())
            .sentAt(event.occurredAt() != null ? event.occurredAt() : now)
            .updatedAt(now)
            .build();
    }
}
