-- ============================================================
-- Shoppiq
-- V24__verification_codes.sql
--
-- Creates the verification_codes table for OTP-based email
-- verification and password reset. Includes expiry, attempt
-- counter for brute-force protection, and used flag.
-- user_id FK cascades on delete.
-- ============================================================

CREATE TABLE verification_codes
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT                  NOT NULL,
    code       VARCHAR(10)             NOT NULL,
    email_type VARCHAR(50)             NOT NULL,
    expires_at TIMESTAMP               NOT NULL,
    used       BOOLEAN   DEFAULT FALSE NOT NULL,
    attempts   INT       DEFAULT 0     NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_verification_codes_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_verification_codes_user_id ON verification_codes (user_id);
CREATE INDEX idx_verification_codes_email_type ON verification_codes (email_type);
CREATE INDEX idx_verification_codes_expires_at ON verification_codes (expires_at);
