-- ============================================================
-- Shoppiq
-- V7__payment_schema.sql
--
-- Creates the payments table with a strict 1:1 relationship
-- to orders, tracking the full payment lifecycle.
-- ============================================================

CREATE TABLE payments
(
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    version           BIGINT         NOT NULL DEFAULT 0,
    created_at        DATETIME(6),
    updated_at        DATETIME(6),

    order_id          BIGINT         NOT NULL,

    payment_reference VARCHAR(50)    NOT NULL,
    payment_method    VARCHAR(20)    NOT NULL,
    payment_status    VARCHAR(20)    NOT NULL,
    gateway           VARCHAR(20)    NOT NULL,

    amount            DECIMAL(12, 2) NOT NULL,
    currency          VARCHAR(10)    NOT NULL DEFAULT 'INR',

    transaction_id    VARCHAR(100),
    gateway_response  TEXT,
    paid_at           DATETIME(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_payments_order
        UNIQUE (order_id),

    CONSTRAINT uk_payments_reference
        UNIQUE (payment_reference),

    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
            ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_payments_transaction_id
    ON payments (transaction_id);

CREATE INDEX idx_payments_status
    ON payments (payment_status);
