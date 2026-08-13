-- ============================================================================
-- CaterHub Worker Marketplace Roles
-- Version: 1.0.0
-- Description: Adds worker marketplace role required for worker onboarding and assignments.
-- ============================================================================

INSERT INTO roles (name, description) VALUES
('ROLE_WORKER', 'Worker role for marketplace job registration, assignment, and job execution')
ON DUPLICATE KEY UPDATE description = VALUES(description);

