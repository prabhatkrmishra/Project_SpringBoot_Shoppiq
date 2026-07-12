-- ============================================================
-- Shoppiq
-- V28__homepage_banners.sql
--
-- CMS-managed sales/offers banners for the homepage.
-- ============================================================

CREATE TABLE homepage_banners
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    version       BIGINT       NOT NULL DEFAULT 0,
    created_at    DATETIME(6),
    updated_at    DATETIME(6),

    badge_text    VARCHAR(50)  NOT NULL,
    badge_type    VARCHAR(20)  NOT NULL,
    heading       VARCHAR(100) NOT NULL,
    body_text     VARCHAR(255),
    button_text   VARCHAR(50),
    button_link   VARCHAR(500),
    heading_color VARCHAR(7)   NOT NULL DEFAULT '#FFFFFF',
    body_color    VARCHAR(30)  NOT NULL DEFAULT 'rgba(255,255,255,0.85)',
    display_order INT          NOT NULL DEFAULT 0,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_homepage_banners PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE INDEX idx_homepage_banners_active_order ON homepage_banners (active, display_order);

-- Seed the 3 existing hardcoded banners
INSERT INTO homepage_banners (badge_text, badge_type, heading, body_text, button_text, button_link, display_order)
VALUES ('Limited Time', 'PRIMARY', 'Up to 50% Off', 'Grab today''s hottest deals before they''re gone.', 'Shop Sale',
        '/sale', 1),
       ('Just In', 'SECONDARY', 'New Arrivals', 'Fresh drops just landed. Stay ahead of the curve.', 'Explore',
        '/new-arrivals', 2),
       ('Perks', 'ACCENT', 'Free Shipping', 'On all orders over $50. No codes needed.', NULL, NULL, 3);
