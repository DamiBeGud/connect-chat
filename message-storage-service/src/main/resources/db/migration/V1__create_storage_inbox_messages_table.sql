CREATE TABLE storage_inbox_messages (
    id UUID PRIMARY KEY,
    source_message_id UUID UNIQUE,
    sender_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    content VARCHAR(4000) NOT NULL,
    event_occurred_at TIMESTAMPTZ,
    status VARCHAR(32) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    locked_at TIMESTAMPTZ,
    processed_at TIMESTAMPTZ,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_storage_inbox_messages_status_created_at
    ON storage_inbox_messages (status, created_at);
