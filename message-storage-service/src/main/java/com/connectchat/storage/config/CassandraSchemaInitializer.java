package com.connectchat.storage.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.stereotype.Component;

@Component
public class CassandraSchemaInitializer {

    public CassandraSchemaInitializer(CqlSession cqlSession) {
        cqlSession.execute(
            """
            CREATE TABLE IF NOT EXISTS messages_by_id (
                message_id uuid PRIMARY KEY,
                source_inbox_message_id uuid,
                sender_id uuid,
                recipient_id uuid,
                content text,
                status text,
                sent_at timestamp,
                updated_at timestamp
            )
            """
        );
        cqlSession.execute(
            """
            CREATE TABLE IF NOT EXISTS undelivered_messages_by_recipient (
                recipient_id uuid,
                sent_at timestamp,
                message_id uuid,
                sender_id uuid,
                content text,
                status text,
                updated_at timestamp,
                PRIMARY KEY ((recipient_id), sent_at, message_id)
            ) WITH CLUSTERING ORDER BY (sent_at ASC, message_id ASC)
            """
        );
        cqlSession.execute(
            """
            CREATE TABLE IF NOT EXISTS group_messages_by_id (
                message_id uuid PRIMARY KEY,
                group_id uuid,
                sender_id uuid,
                content text,
                sent_at timestamp,
                updated_at timestamp
            )
            """
        );
        cqlSession.execute(
            """
            CREATE TABLE IF NOT EXISTS undelivered_group_messages_by_recipient (
                recipient_id uuid,
                sent_at timestamp,
                message_id uuid,
                group_id uuid,
                sender_id uuid,
                content text,
                status text,
                updated_at timestamp,
                PRIMARY KEY ((recipient_id), sent_at, message_id)
            ) WITH CLUSTERING ORDER BY (sent_at ASC, message_id ASC)
            """
        );
    }
}
