-- Client-generated idempotency key so a retried booking submission (lost response,
-- double tap, app restart) returns the original booking instead of creating a duplicate.
-- Additive and nullable: existing rows and older app versions are unaffected.
ALTER TABLE service_requests
    ADD COLUMN client_request_id VARCHAR(64) NULL AFTER created_by,
    ADD UNIQUE INDEX uk_service_requests_client_request (created_by, client_request_id);
