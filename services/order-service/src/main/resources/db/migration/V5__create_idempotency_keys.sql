CREATE TABLE idempotency_keys (
    id UUID PRIMARY KEY,
    key VARCHAR(255) NOT NULL,
    customer_id UUID NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_status INTEGER,
    response_body TEXT,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_idempotency_keys_customer_key
        UNIQUE (customer_id, key)
);

CREATE INDEX idx_idempotency_keys_customer_key
    ON idempotency_keys (customer_id, key);

CREATE INDEX idx_idempotency_keys_expires_at
    ON idempotency_keys (expires_at);