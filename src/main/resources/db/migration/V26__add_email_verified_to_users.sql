-- V26__add_email_verified_to_users.sql
-- Add email verification status to users table

ALTER TABLE users ADD COLUMN email_verified BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE users ADD COLUMN email_verified_at TIMESTAMP NULL;
