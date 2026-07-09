-- ============================================================
-- Shoppiq
-- V21__notification_preferences.sql
--
-- Per-user email notification preferences.
-- ============================================================

CREATE TABLE notification_preferences
(
    id                 BIGINT  NOT NULL AUTO_INCREMENT,
    version            BIGINT  NOT NULL,
    created_at         DATETIME(6),
    updated_at         DATETIME(6),

    user_id            BIGINT  NOT NULL,
    order_updates      BOOLEAN NOT NULL DEFAULT TRUE,
    account_security   BOOLEAN NOT NULL DEFAULT TRUE,
    promotions         BOOLEAN NOT NULL DEFAULT TRUE,
    reviews_engagement BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_notification_preferences PRIMARY KEY (id),
    CONSTRAINT fk_notification_preferences_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_notification_preferences_user UNIQUE (user_id)
) ENGINE = InnoDB;
