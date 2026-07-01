-- ============================================================
-- Shoppiq
-- V6__order_schema.sql
--
-- Redesigned orders table (snapshot model) and order_items table.
-- Drops the legacy orders / order_with_items tables if they exist.
-- ============================================================

-- Drop legacy join table first (FK dependent)
DROP TABLE IF EXISTS order_with_items;

-- Drop legacy orders table
DROP TABLE IF EXISTS orders;

-- ============================================================
-- orders
-- ============================================================
CREATE TABLE orders
(
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    version        BIGINT         NOT NULL DEFAULT 0,
    created_at     DATETIME(6),
    updated_at     DATETIME(6),

    user_id        BIGINT         NOT NULL,
    address_id     BIGINT         NOT NULL,

    status         VARCHAR(30)    NOT NULL,
    payment_method VARCHAR(20)    NOT NULL,
    payment_status VARCHAR(20)    NOT NULL,

    subtotal       DECIMAL(12, 2) NOT NULL,
    shipping_fee   DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    tax            DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount       DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    grand_total    DECIMAL(12, 2) NOT NULL,

    placed_at      DATETIME(6)    NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_orders_address
        FOREIGN KEY (address_id) REFERENCES addresses (id)
            ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX idx_orders_user
    ON orders (user_id);

CREATE INDEX idx_orders_user_placed_at
    ON orders (user_id, placed_at DESC);

-- ============================================================
-- order_items
-- ============================================================
CREATE TABLE order_items
(
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    order_id            BIGINT         NOT NULL,
    item_details_id     BIGINT,

    item_name_snapshot  VARCHAR(150)   NOT NULL,
    unit_price_snapshot DECIMAL(10, 2) NOT NULL,
    quantity            INT            NOT NULL,
    subtotal            DECIMAL(12, 2) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_order_items_item_details
        FOREIGN KEY (item_details_id) REFERENCES item_details (id)
            ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE INDEX idx_order_items_order
    ON order_items (order_id);
