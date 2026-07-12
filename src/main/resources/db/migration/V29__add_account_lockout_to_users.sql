-- ============================================================
-- Shoppiq
-- V29__add_account_lockout_to_users.sql
--
-- Account lockout support for brute-force protection.
-- Tracks consecutive failed login attempts and the timestamp
-- when the account was locked. Accounts auto-unlock after
-- 30 minutes (soft lockout enforced at the application layer).
-- ============================================================

ALTER TABLE users
    ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN lockout_time          DATETIME NULL;
