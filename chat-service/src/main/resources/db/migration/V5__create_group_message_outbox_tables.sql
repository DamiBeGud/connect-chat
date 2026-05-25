CREATE TABLE group_outbox_messages (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    content VARCHAR(4000) NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    locked_at TIMESTAMPTZ,
    processed_at TIMESTAMPTZ,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_group_outbox_messages_status_created_at
    ON group_outbox_messages (status, created_at);

CREATE TABLE group_outbox_recipients (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_group_outbox_recipients_message_recipient
        UNIQUE (message_id, recipient_id)
);

CREATE INDEX idx_group_outbox_recipients_message_created_at
    ON group_outbox_recipients (message_id, created_at);
