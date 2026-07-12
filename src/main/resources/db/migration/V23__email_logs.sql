-- ============================================================
-- Shoppiq
-- V23__email_logs.sql
--
-- Creates the email_logs table for auditing all outgoing emails.
-- Records recipient, email type, delivery status, provider
-- response, and error messages. user_id FK is SET NULL on
-- delete to retain logs for deleted accounts.
-- ============================================================

CREATE TABLE email_logs
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT,
    email_type      VARCHAR(50)  NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    subject         VARCHAR(255) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    provider        VARCHAR(50),
    error_message   TEXT,
    sent_at         TIMESTAMP NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_email_logs_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE SET NULL
);

CREATE INDEX idx_email_logs_user_id ON email_logs (user_id);
CREATE INDEX idx_email_logs_email_type ON email_logs (email_type);
CREATE INDEX idx_email_logs_status ON email_logs (status);
CREATE INDEX idx_email_logs_created_at ON email_logs (created_at);
