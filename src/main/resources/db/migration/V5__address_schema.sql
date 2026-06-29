-- ============================================================
-- Shoppiq
-- V5__address_schema.sql
--
-- Addresses table for user shipping address management.
-- ============================================================

CREATE TABLE addresses
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    version     BIGINT       NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),

    user_id     BIGINT       NOT NULL,
    label       VARCHAR(30)  NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    phone       VARCHAR(15)  NOT NULL,
    line1       VARCHAR(255) NOT NULL,
    line2       VARCHAR(255),
    city        VARCHAR(100) NOT NULL,
    state       VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10)  NOT NULL,
    country     VARCHAR(100) NOT NULL,
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE,

    PRIMARY KEY (id),

    CONSTRAINT fk_addresses_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_addresses_user
    ON addresses (user_id);

CREATE INDEX idx_addresses_user_default
    ON addresses (user_id, is_default);
