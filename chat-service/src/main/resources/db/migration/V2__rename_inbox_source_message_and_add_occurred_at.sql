ALTER TABLE inbox_messages
    RENAME COLUMN source_outbox_message_id TO source_message_id;

ALTER TABLE inbox_messages
    ADD COLUMN occurred_at TIMESTAMPTZ;
