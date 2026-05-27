ALTER TABLE outbox_messages
    ALTER COLUMN content TYPE VARCHAR(16000);

ALTER TABLE inbox_messages
    ALTER COLUMN content TYPE VARCHAR(16000);

ALTER TABLE group_outbox_messages
    ALTER COLUMN content TYPE VARCHAR(16000);
