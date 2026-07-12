-- ============================================================
-- Shoppiq
-- V10__add_seller_id_to_items.sql
--
-- Adds seller_id FK to items for marketplace seller ownership.
-- ============================================================

ALTER TABLE items
    ADD COLUMN seller_id BIGINT AFTER description,
    ADD CONSTRAINT fk_items_seller
        FOREIGN KEY (seller_id)
            REFERENCES sellers (id)
            ON
DELETE
SET NULL;

CREATE INDEX idx_items_seller
    ON items (seller_id);
