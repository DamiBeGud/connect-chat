CREATE TABLE websocket_delivery_tasks (
    id UUID PRIMARY KEY,
    source_event_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    target_user_id UUID NOT NULL,
    target_session_id VARCHAR(255) NOT NULL,
    target_instance_id VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    locked_at TIMESTAMPTZ,
    processed_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ NOT NULL,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_websocket_delivery_tasks_source_type_session_destination
        UNIQUE (source_event_id, type, target_session_id, destination)
);

CREATE INDEX idx_websocket_delivery_tasks_instance_status_created_at
    ON websocket_delivery_tasks (target_instance_id, status, created_at);

CREATE INDEX idx_websocket_delivery_tasks_expires_at
    ON websocket_delivery_tasks (expires_at);
