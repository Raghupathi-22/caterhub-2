-- ============================================================================
-- Worker self-service profile editing: optional age.
-- Nullable so existing worker profiles remain valid without a backfill.
-- Range (18-75) is enforced by the application layer.
-- ============================================================================

ALTER TABLE worker_profiles
    ADD COLUMN age INT NULL AFTER experience_years;
