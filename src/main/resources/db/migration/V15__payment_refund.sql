-- ============================================================
-- Shoppiq
-- V15__payment_refund.sql
--
-- Adds the refunded_at column to complete the payment lifecycle
-- audit trail (pending -> processing -> paid -> refunded).
-- ============================================================

ALTER TABLE payments
    ADD COLUMN refunded_at DATETIME(6) NULL;
