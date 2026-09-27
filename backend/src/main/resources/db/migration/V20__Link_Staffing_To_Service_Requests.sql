ALTER TABLE staffing_requests
    ADD COLUMN service_request_id BIGINT NULL AFTER created_by,
    ADD INDEX idx_staffing_requests_service_request (service_request_id),
    ADD CONSTRAINT fk_staffing_requests_service_request
        FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE SET NULL;
