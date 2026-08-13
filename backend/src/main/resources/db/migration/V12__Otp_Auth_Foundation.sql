-- Strengthen OTP storage for mobile-based authentication

ALTER TABLE otps
    ADD COLUMN requester_ip VARCHAR(45) NULL AFTER purpose,
    ADD COLUMN verified_at DATETIME NULL AFTER expires_at,
    ADD COLUMN last_attempt_at DATETIME NULL AFTER attempts;

CREATE INDEX idx_otp_requester_ip ON otps (requester_ip);
CREATE INDEX idx_otp_mobile_purpose_created_at ON otps (mobile_number, purpose, created_at);
