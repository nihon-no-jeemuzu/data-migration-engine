CREATE TABLE migration_item_error_log (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    legacy_id VARCHAR(255),
    raw_payload TEXT,
    error_reason TEXT,
    failed_at TIMESTAMP
);