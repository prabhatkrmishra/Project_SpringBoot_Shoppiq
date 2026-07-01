-- Seed roles
INSERT INTO roles (role_name)
VALUES ('ROLE_ADMIN');
INSERT INTO roles (role_name)
VALUES ('ROLE_CUSTOMER');
INSERT INTO roles (role_name)
VALUES ('ROLE_SELLER');

-- Seed a customer user
INSERT INTO users (name, email, username, password, token_version, enabled, created_at, updated_at)
VALUES ('Test User', 'test@example.com', 'testuser', '$2a$10$dummyhash', 0, true, NOW(), NOW());

-- Seed a seller user
INSERT INTO users (name, email, username, password, token_version, enabled, created_at, updated_at)
VALUES ('Seller User', 'seller@example.com', 'selleruser', '$2a$10$dummyhash', 0, true, NOW(), NOW());

-- Assign CUSTOMER role to test user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u,
     roles r
WHERE u.email = 'test@example.com'
  AND r.role_name = 'ROLE_CUSTOMER';

-- Assign CUSTOMER + SELLER roles to seller user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u,
     roles r
WHERE u.email = 'seller@example.com'
  AND r.role_name = 'ROLE_CUSTOMER';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u,
     roles r
WHERE u.email = 'seller@example.com'
  AND r.role_name = 'ROLE_SELLER';

-- Seed a category
INSERT INTO categories (name, created_at, updated_at)
VALUES ('Test Category', NOW(), NOW());

-- Seed an address
INSERT INTO addresses (user_id, full_name, street, city, state, zip_code, country, is_default, created_at, updated_at)
VALUES (1, 'Test User', '123 Test St', 'Test City', 'Test State', '12345', 'IN', true, NOW(), NOW());

-- Seed a seller (PENDING)
INSERT INTO sellers (user_id, business_name, business_email, phone, gst_number, pan_number, business_address_id,
                     verification_status, seller_status, commission_rate, rating, joined_at, created_at, updated_at)
VALUES (2, 'Test Business', 'business@example.com', '9999999999', 'GST123', 'PAN12345', 1, 'PENDING', 'INACTIVE', 5.00,
        0.00, NOW(), NOW(), NOW());
