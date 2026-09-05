-- ============================================================================
-- CaterHub worker type column hardening
-- Converts worker_type enum columns to VARCHAR to avoid runtime truncation when
-- role catalog expands faster than deployed enum definitions.
-- ============================================================================

ALTER TABLE worker_profiles
    MODIFY COLUMN worker_type VARCHAR(64) NOT NULL;

ALTER TABLE staffing_requests
    MODIFY COLUMN worker_type VARCHAR(64) NOT NULL;

ALTER TABLE job_assignments
    MODIFY COLUMN worker_type VARCHAR(64) NOT NULL;
