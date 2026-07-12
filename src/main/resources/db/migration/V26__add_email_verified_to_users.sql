-- ============================================================
-- Shoppiq
-- V26__add_email_verified_to_users.sql
--
-- Adds email_verified flag and email_verified_at timestamp to
-- users table. Set to TRUE with timestamp upon successful OTP
-- validation during registration.
-- ============================================================

ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE users
    ADD COLUMN email_verified_at TIMESTAMP NULL;
