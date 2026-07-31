-- ============================================================
-- Shoppiq
-- V35__entity_type_fixes_and_role_audit.sql
--
-- Fixes column type/constraint mismatches between JPA entity
-- definitions and the physical schema, and adds standard
-- audit + optimistic-locking columns to the roles table.
--
-- What changes:
--   1. sellers.joined_at  → TIMESTAMP(6) for sub-second precision
--   2. item_details.on_sale → NOT NULL with default 0
--      (backfills NULL rows first)
--   3. cart_items.quantity → NOT NULL with default 1
--      (backfills NULL rows first)
--   4. roles.created_at, roles.updated_at, roles.version
--      added idempotently via information_schema column check
--      so the migration is re-entrant if partially applied.
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- sellers: widen joined_at precision to match Instant mapping
-- ────────────────────────────────────────────────────────────
ALTER TABLE sellers
    MODIFY COLUMN joined_at TIMESTAMP (6) NULL;

-- ────────────────────────────────────────────────────────────
-- item_details: make on_sale non-nullable
-- ────────────────────────────────────────────────────────────
UPDATE item_details
SET on_sale = 0
WHERE on_sale IS NULL;

ALTER TABLE item_details
    MODIFY COLUMN on_sale tinyint NOT NULL DEFAULT 0;

-- ────────────────────────────────────────────────────────────
-- cart_items: make quantity non-nullable
-- ────────────────────────────────────────────────────────────
UPDATE cart_items
SET quantity = 1
WHERE quantity IS NULL;

ALTER TABLE cart_items
    MODIFY COLUMN quantity INT NOT NULL DEFAULT 1;

-- ────────────────────────────────────────────────────────────
-- roles: add standard audit and optimistic-locking columns
-- each column is created only if it does not already exist
-- ────────────────────────────────────────────────────────────
DELIMITER
//
CREATE PROCEDURE IF NOT EXISTS add_col_if_missing(
    IN p_table VARCHAR(255),
    IN p_col   VARCHAR(255),
    IN p_def   VARCHAR(255)
)
BEGIN
    IF
NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = p_table
          AND COLUMN_NAME  = p_col
    ) THEN
        SET @s = CONCAT('ALTER TABLE ', p_table, ' ADD COLUMN ', p_col, ' ', p_def);
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END IF;
END
//
DELIMITER ;

CALL add_col_if_missing('roles', 'created_at', 'DATETIME(6) NULL');
CALL add_col_if_missing('roles', 'updated_at', 'DATETIME(6) NULL');
CALL add_col_if_missing('roles', 'version',    'BIGINT NOT NULL DEFAULT 0');
