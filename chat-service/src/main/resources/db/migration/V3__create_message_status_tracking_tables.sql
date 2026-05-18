CREATE TABLE message_status_outbox_events (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    status_value VARCHAR(32) NOT NULL,
    actor_user_id UUID NOT NULL,
    event_occurred_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    locked_at TIMESTAMPTZ,
    processed_at TIMESTAMPTZ,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_message_status_outbox_events_status_created_at
    ON message_status_outbox_events (status, created_at);

CREATE TABLE message_status_inbox_events (
    id UUID PRIMARY KEY,
    source_event_id UUID NOT NULL UNIQUE,
    message_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    status_value VARCHAR(32) NOT NULL,
    actor_user_id UUID,
    event_occurred_at TIMESTAMPTZ,
    status VARCHAR(32) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    locked_at TIMESTAMPTZ,
    processed_at TIMESTAMPTZ,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_message_status_inbox_events_status_created_at
    ON message_status_inbox_events (status, created_at);
