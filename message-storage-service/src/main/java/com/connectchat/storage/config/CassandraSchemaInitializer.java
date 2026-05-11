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
    }
}
