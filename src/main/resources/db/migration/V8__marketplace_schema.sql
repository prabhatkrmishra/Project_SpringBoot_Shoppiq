-- ============================================================
-- Shoppiq
-- V8__marketplace_schema.sql
--
-- Marketplace transformation: sellers, stores, and address
-- relaxation to support owner-agnostic address references.
-- ============================================================

-- ------------------------------------------------------------
-- 1. Relax address user_id constraint
-- ------------------------------------------------------------
-- Make user_id nullable so addresses can be referenced by
-- sellers and stores without being owned by a user.

ALTER TABLE addresses
    MODIFY user_id BIGINT NULL;

-- ------------------------------------------------------------
-- 2. Sellers table
-- ------------------------------------------------------------

CREATE TABLE sellers
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    version             BIGINT       NOT NULL,
    created_at          DATETIME(6),
    updated_at          DATETIME(6),

    user_id             BIGINT,
    business_name       VARCHAR(255) NOT NULL,
    business_email      VARCHAR(255) NOT NULL,
    phone               VARCHAR(15)  NOT NULL,
    gst_number          VARCHAR(20),
    pan_number          VARCHAR(10)  NOT NULL,
    business_address_id BIGINT,
    verification_status VARCHAR(20)  NOT NULL,
    seller_status       VARCHAR(20)  NOT NULL,
    commission_rate     DECIMAL(5, 2),
    rating              DECIMAL(3, 2),
    joined_at           DATETIME(6),

    PRIMARY KEY (id),

    CONSTRAINT uq_sellers_user
        UNIQUE (user_id),
    CONSTRAINT uq_sellers_business_email
        UNIQUE (business_email),
    CONSTRAINT fk_sellers_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_sellers_business_address
        FOREIGN KEY (business_address_id)
            REFERENCES addresses (id)
            ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE INDEX idx_sellers_verification_status
    ON sellers (verification_status);

CREATE INDEX idx_sellers_seller_status
    ON sellers (seller_status);

-- ------------------------------------------------------------
-- 3. Stores table
-- ------------------------------------------------------------

CREATE TABLE stores
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    version     BIGINT       NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),

    seller_id   BIGINT       NOT NULL,
    store_name  VARCHAR(255) NOT NULL,
    slug        VARCHAR(100) NOT NULL,
    description TEXT,
    logo        VARCHAR(500),
    banner      VARCHAR(500),
    status      VARCHAR(20)  NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uq_stores_seller
        UNIQUE (seller_id),
    CONSTRAINT uq_stores_slug
        UNIQUE (slug),
    CONSTRAINT fk_stores_seller
        FOREIGN KEY (seller_id)
            REFERENCES sellers (id)
            ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_stores_slug
    ON stores (slug);

CREATE INDEX idx_stores_status
    ON stores (status);
