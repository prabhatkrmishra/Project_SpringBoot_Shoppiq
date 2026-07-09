-- ============================================================
-- Shoppiq
-- V22__promo_code_schema.sql
--
-- Introduces promo codes (coupons) and per-user usage tracking.
-- Adds a nullable promo_code_id FK to orders.
-- ============================================================

-- ============================================================
-- promo_codes
-- ============================================================
CREATE TABLE promo_codes
(
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    version             BIGINT         NOT NULL DEFAULT 0,
    created_at          DATETIME(6),
    updated_at          DATETIME(6),

    code                VARCHAR(50)    NOT NULL,
    description         VARCHAR(255),
    discount_type       VARCHAR(20)    NOT NULL,
    discount_value      DECIMAL(10, 2) NOT NULL,
    min_order_amount    DECIMAL(10, 2),
    max_discount_amount DECIMAL(10, 2),
    usage_limit         INT,
    used_count          INT            NOT NULL DEFAULT 0,
    user_usage_limit    INT,
    valid_from          DATETIME(6)    NOT NULL,
    valid_until         DATETIME(6)    NOT NULL,
    active              BOOLEAN        NOT NULL DEFAULT TRUE,

    PRIMARY KEY (id),

    CONSTRAINT uk_promo_codes_code UNIQUE (code)
) ENGINE = InnoDB;

CREATE INDEX idx_promo_codes_active
    ON promo_codes (active);

CREATE INDEX idx_promo_codes_valid_window
    ON promo_codes (valid_from, valid_until);

-- ============================================================
-- promo_code_usage
-- ============================================================
CREATE TABLE promo_code_usage
(
    id            BIGINT NOT NULL AUTO_INCREMENT,
    version       BIGINT NOT NULL DEFAULT 0,
    created_at    DATETIME(6),
    updated_at    DATETIME(6),

    promo_code_id BIGINT NOT NULL,
    user_id       BIGINT NOT NULL,
    order_id      BIGINT NOT NULL,
    used_at       DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_promo_usage_promo_code
        FOREIGN KEY (promo_code_id) REFERENCES promo_codes (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_promo_usage_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_promo_usage_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_promo_usage_order UNIQUE (order_id)
) ENGINE = InnoDB;

CREATE INDEX idx_promo_usage_promo_code
    ON promo_code_usage (promo_code_id);

CREATE INDEX idx_promo_usage_user
    ON promo_code_usage (user_id);

-- ============================================================
-- Add promo_code_id to orders (nullable)
-- ============================================================
ALTER TABLE orders
    ADD COLUMN promo_code_id BIGINT;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_promo_code
        FOREIGN KEY (promo_code_id) REFERENCES promo_codes (id)
            ON DELETE SET NULL;

CREATE INDEX idx_orders_promo_code
    ON orders (promo_code_id);
