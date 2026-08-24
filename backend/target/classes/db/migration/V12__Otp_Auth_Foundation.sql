-- Restructure the otps table for mobile OTP authentication.
--
-- Background: V1 created an 'otps' table for legacy phone/email OTP verification
-- (columns: phone_number, otp_code, otp_type, is_verified, attempt_count, verified_at).
-- V11 attempted to replace it using CREATE TABLE IF NOT EXISTS, but because V1's table
-- already existed, MySQL silently skipped that CREATE statement while still marking
-- V11 as successful. The legacy V1 schema was therefore never replaced.
--
-- V12 (original) then attempted to ALTER TABLE otps with ADD COLUMN referencing
-- columns that only exist in the V11 schema (purpose, attempts), causing the entire
-- ALTER TABLE to fail atomically. Nothing was changed; Flyway recorded V12 as FAILED.
--
-- This corrected V12 drops the stale V1-schema otps table and recreates it with the
-- exact schema that OtpEntity.java and OtpRepository require.
--
-- Safety:
--   - OTP records expire in 5 minutes; no business-critical data resides in this table.
--   - The application has been offline since V12 failed, so no live OTP sessions exist.
--   - No other table references otps via a foreign key (verified across V1-V11).

DROP TABLE IF EXISTS otps;

CREATE TABLE otps (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    mobile_number   VARCHAR(20)  NOT NULL,
    otp_hash        VARCHAR(255) NOT NULL,
    purpose         VARCHAR(50)  NOT NULL,
    requester_ip    VARCHAR(45)  NULL,
    attempts        INT          NOT NULL DEFAULT 0,
    max_attempts    INT          NOT NULL DEFAULT 5,
    is_used         BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at      DATETIME     NOT NULL,
    verified_at     DATETIME     NULL,
    last_attempt_at DATETIME     NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_otp_mobile                    (mobile_number),
    INDEX idx_otp_expires                   (expires_at),
    INDEX idx_otp_requester_ip              (requester_ip),
    INDEX idx_otp_mobile_purpose_created_at (mobile_number, purpose, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
