-- ============================================================
-- Shoppiq
-- V11__seed_shoppiq_official_seller.sql
--
-- Creates the "Shoppiq Official" system seller and assigns all
-- legacy items (those without a seller) to it.
-- ============================================================

-- ------------------------------------------------------------
-- 1. Create the system user for "Shoppiq Official"
-- ------------------------------------------------------------
SET
@systemEmail = 'system@shoppiq.com';

INSERT INTO users (version, created_at, updated_at, name, email, username, password, token_version, enabled)
SELECT 0,
       NOW(6),
       NOW(6),
       'Shoppiq Official',
       @systemEmail,
       'system_shoppiq',
       NULL,
       0,
       1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = @systemEmail);

-- ------------------------------------------------------------
-- 2. Assign the ROLE_SELLER role to the system user
-- ------------------------------------------------------------
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u,
     roles r
WHERE u.email = @systemEmail
  AND r.role_name = 'ROLE_SELLER'
  AND NOT EXISTS (SELECT 1
                  FROM user_roles ur
                  WHERE ur.user_id = u.id
                    AND ur.role_id = r.id);

-- ------------------------------------------------------------
-- 3. Create the "Shoppiq Official" seller
-- ------------------------------------------------------------
SET
@systemUserId = (SELECT id FROM users WHERE email = @systemEmail);

INSERT INTO sellers (version, created_at, updated_at, user_id, business_name, business_email, phone, pan_number,
                     verification_status, seller_status, joined_at)
SELECT 0,
       NOW(6),
       NOW(6),
       @systemUserId,
       'Shoppiq Official',
       @systemEmail,
       '0000000000',
       'ABCDE1234F',
       'APPROVED',
       'ACTIVE',
       NOW(6)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sellers WHERE business_email = @systemEmail);

-- ------------------------------------------------------------
-- 4. Create the "Shoppiq Official" store
-- ------------------------------------------------------------
SET
@officialSellerId = (SELECT id FROM sellers WHERE business_email = @systemEmail);

INSERT INTO stores (version, created_at, updated_at, seller_id, store_name, slug, status)
SELECT 0, NOW(6), NOW(6), @officialSellerId, 'Shoppiq Official', 'shoppiq-official', 'PUBLISHED'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM stores WHERE slug = 'shoppiq-official');

-- ------------------------------------------------------------
-- 5. Assign all legacy items to the Shoppiq Official seller
-- ------------------------------------------------------------
UPDATE items
SET seller_id = @officialSellerId
WHERE seller_id IS NULL;
