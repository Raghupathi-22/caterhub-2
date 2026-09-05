ALTER TABLE service_requests
    ADD COLUMN end_time TIME NULL AFTER start_time,
    ADD COLUMN selected_services TEXT NULL AFTER area,
    ADD COLUMN instructions TEXT NULL AFTER selected_services,
    ADD COLUMN quote_based BOOLEAN NOT NULL DEFAULT FALSE AFTER details;

