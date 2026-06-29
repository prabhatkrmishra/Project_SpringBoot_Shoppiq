-- ============================================================
-- Shoppiq
-- V4__cart_schema.sql
--
-- Cart and CartItem tables.
-- ============================================================

-- ============================================================
-- Cart
-- ============================================================

CREATE TABLE cart
(
    id         BIGINT NOT NULL AUTO_INCREMENT,
    version    BIGINT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),

    user_id    BIGINT NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_cart_user
        UNIQUE (user_id),

    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_cart_user
    ON cart (user_id);

-- ============================================================
-- Cart Items
-- ============================================================

CREATE TABLE cart_items
(
    id       BIGINT  NOT NULL AUTO_INCREMENT,
    cart_id  BIGINT  NOT NULL,
    item_details_id BIGINT  NOT NULL,
    quantity INTEGER NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT chk_cart_item_quantity
        CHECK (quantity > 0),

    CONSTRAINT uk_cart_items_cart_item
        UNIQUE (cart_id, item_details_id),

    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id)
            REFERENCES cart (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_cart_items_item_details
        FOREIGN KEY (item_details_id)
            REFERENCES item_details (id)
) ENGINE = InnoDB;

CREATE INDEX idx_cart_items_cart
    ON cart_items (cart_id);

CREATE INDEX idx_cart_items_item_details
    ON cart_items (item_details_id);
