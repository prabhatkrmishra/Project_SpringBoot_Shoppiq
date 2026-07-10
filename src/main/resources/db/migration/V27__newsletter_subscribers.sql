-- ============================================================
-- Shoppiq
-- V27__newsletter_subscribers.sql
--
-- Newsletter subscribers for non-registered users.
-- ============================================================

CREATE TABLE newsletter_subscribers
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),

    email           VARCHAR(255) NOT NULL,
    token           VARCHAR(36)  NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    subscribed_at   DATETIME(6)  NOT NULL,
    unsubscribed_at DATETIME(6),

    CONSTRAINT pk_newsletter_subscribers PRIMARY KEY (id),
    CONSTRAINT uk_newsletter_subscribers_email UNIQUE (email),
    CONSTRAINT uk_newsletter_subscribers_token UNIQUE (token)
) ENGINE = InnoDB;

CREATE INDEX idx_newsletter_subscribers_active ON newsletter_subscribers (active);
