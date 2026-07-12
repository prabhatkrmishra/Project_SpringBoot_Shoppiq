-- ============================================================
-- Shoppiq
-- V1__baseline.sql
--
-- Initial database schema: users, roles, categories, items,
-- item_details, and the user-role join table.
-- ============================================================

-- ============================================================
-- Categories
-- ============================================================

CREATE TABLE categories
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    version     BIGINT       NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),

    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL,
    description VARCHAR(255),

    PRIMARY KEY (id),

    CONSTRAINT uk_categories_name UNIQUE (name),
    CONSTRAINT uk_categories_slug UNIQUE (slug)
) ENGINE=InnoDB;

-- ============================================================
-- Roles
-- ============================================================

CREATE TABLE roles
(
    id        BIGINT       NOT NULL AUTO_INCREMENT,

    role_name VARCHAR(255) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_roles_name UNIQUE (role_name)
) ENGINE=InnoDB;

-- ============================================================
-- Users
-- ============================================================

CREATE TABLE users
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    version       BIGINT       NOT NULL,
    created_at    DATETIME(6),
    updated_at    DATETIME(6),

    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    username      VARCHAR(50)  NOT NULL,
    password      VARCHAR(255),

    token_version INT          NOT NULL,
    enabled       BIT          NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_username UNIQUE (username)
) ENGINE=InnoDB;

-- ============================================================
-- User Roles
-- ============================================================

CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
) ENGINE=InnoDB;

CREATE INDEX idx_user_roles_user
    ON user_roles (user_id);

CREATE INDEX idx_user_roles_role
    ON user_roles (role_id);

-- ============================================================
-- Item Details
-- ============================================================

CREATE TABLE item_details
(
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    version             BIGINT         NOT NULL,
    created_at          DATETIME(6),
    updated_at          DATETIME(6),

    brand               VARCHAR(100)   NOT NULL,
    sku                 VARCHAR(100)   NOT NULL,

    price               DECIMAL(12, 2) NOT NULL,
    stock_quantity      INT            NOT NULL,
    discount_percentage DECIMAL(5, 2)  NOT NULL,

    category_id         BIGINT         NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_item_details_sku
        UNIQUE (sku),

    CONSTRAINT fk_item_details_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id)
) ENGINE=InnoDB;

CREATE INDEX idx_item_details_category
    ON item_details (category_id);

-- ============================================================
-- Items
-- ============================================================

CREATE TABLE items
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    version         BIGINT       NOT NULL,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),

    name            VARCHAR(150) NOT NULL,
    description     VARCHAR(500) NOT NULL,

    item_details_id BIGINT       NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_items_item_details
        UNIQUE (item_details_id),

    CONSTRAINT fk_items_item_details
        FOREIGN KEY (item_details_id)
            REFERENCES item_details (id)
) ENGINE=InnoDB;

-- ============================================================
-- Item Reviews
-- ============================================================

CREATE TABLE item_reviews
(
    id         BIGINT NOT NULL AUTO_INCREMENT,
    version    BIGINT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),

    item_id    BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,

    rating     INT    NOT NULL,
    review     VARCHAR(1000),

    PRIMARY KEY (id),

    CONSTRAINT chk_item_review_rating
        CHECK (rating BETWEEN 1 AND 5),

    CONSTRAINT fk_item_review_item
        FOREIGN KEY (item_id)
            REFERENCES items (id),

    CONSTRAINT fk_item_review_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT uk_item_review_user_item
        UNIQUE (user_id, item_id)
) ENGINE=InnoDB;

CREATE INDEX idx_item_reviews_item
    ON item_reviews (item_id);

CREATE INDEX idx_item_reviews_user
    ON item_reviews (user_id);

-- ============================================================
-- Orders
-- ============================================================

CREATE TABLE orders
(
    id   BIGINT NOT NULL AUTO_INCREMENT,

    type VARCHAR(255),

    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ============================================================
-- Order Items
-- ============================================================

CREATE TABLE order_with_items
(
    orders_id BIGINT NOT NULL,
    items_id  BIGINT NOT NULL,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (orders_id)
            REFERENCES orders (id),

    CONSTRAINT fk_order_items_item
        FOREIGN KEY (items_id)
            REFERENCES items (id)
) ENGINE=InnoDB;

CREATE INDEX idx_order_items_order
    ON order_with_items (orders_id);

CREATE INDEX idx_order_items_item
    ON order_with_items (items_id);