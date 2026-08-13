-- ============================================================================
-- CaterHub Worker Staffing Jobs
-- Adds staffing requests and worker acceptances with capacity tracking.
-- ============================================================================

CREATE TABLE IF NOT EXISTS staffing_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_by BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    worker_type ENUM('CHEF', 'ASSISTANT_CHEF', 'SERVING_BOY', 'SERVING_GIRL', 'CLEANER', 'KITCHEN_HELPER', 'SUPERVISOR') NOT NULL,
    event_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(255) NOT NULL,
    area VARCHAR(100) NOT NULL,
    required_workers INT NOT NULL,
    accepted_workers INT NOT NULL DEFAULT 0,
    payment DECIMAL(10, 2) NOT NULL,
    additional_requirements TEXT,
    status ENUM('OPEN', 'FILLED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_staffing_requests_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_staffing_required_workers CHECK (required_workers > 0),
    CONSTRAINT chk_staffing_accepted_workers CHECK (accepted_workers >= 0 AND accepted_workers <= required_workers),
    CONSTRAINT chk_staffing_time CHECK (start_time < end_time),
    INDEX idx_staffing_requests_status_role (status, worker_type),
    INDEX idx_staffing_requests_area (area),
    INDEX idx_staffing_requests_event_date (event_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS staffing_job_acceptances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    staffing_request_id BIGINT NOT NULL,
    worker_profile_id BIGINT NOT NULL,
    status ENUM('ACCEPTED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'ACCEPTED',
    accepted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_staffing_acceptance_request FOREIGN KEY (staffing_request_id) REFERENCES staffing_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_staffing_acceptance_worker FOREIGN KEY (worker_profile_id) REFERENCES worker_profiles(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_staffing_acceptance_request_worker (staffing_request_id, worker_profile_id),
    INDEX idx_staffing_acceptances_worker_status (worker_profile_id, status),
    INDEX idx_staffing_acceptances_request_status (staffing_request_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

