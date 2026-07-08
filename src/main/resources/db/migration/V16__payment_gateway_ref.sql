-- ============================================================
-- Shoppiq
-- V16__payment_gateway_ref.sql
--
-- Adds gateway_payment_id to store the gateway-assigned order /
-- payment-intent / collect identifier, enabling reconciliation
-- and idempotent re-initiation of payments.
-- ============================================================

ALTER TABLE payments
    ADD COLUMN gateway_payment_id VARCHAR(100) NULL;

CREATE INDEX idx_payments_gateway_payment_id
    ON payments (gateway_payment_id);
