-- ============================================================
-- V30: AI Chat Assistant — conversation and message tables
-- ============================================================
-- Adds persistence for the LangChain4j-powered AI shopping
-- assistant. Two tables store chat conversations and individual
-- messages so that full history survives JVM restarts and can
-- be reviewed in the admin dashboard.
-- ============================================================
-- Table: chat_conversations
--   Stores one row per AI conversation (authenticated or guest).
--   - chat_id:   Human-readable ID (e.g. CHAT-2026-07-A3F2)
--   - status:    ACTIVE or RESOLVED (auto-resolved after closing intent)
--   - user_id:   FK to users for authenticated conversations (NULL for guests)
--   - guest_session / guest_ip: Track guest conversations by session cookie
-- ============================================================
-- Table: chat_messages
--   Stores each message within a conversation.
--   - role:      USER, ASSISTANT, or SYSTEM
--   - content:   The full message text
--   - tool_name: Name of the tool invoked by the assistant (if any)
--   - tokens_used: Token count for cost tracking (nullable)
-- ============================================================

CREATE TABLE chat_conversations
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    version       BIGINT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id       BIGINT NULL,
    chat_id       VARCHAR(20)  NOT NULL,
    title         VARCHAR(255) NOT NULL DEFAULT 'New Conversation',
    status        VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    resolved_at   TIMESTAMP NULL,
    guest_session VARCHAR(64) NULL,
    guest_ip      VARCHAR(45) NULL,
    CONSTRAINT fk_chat_conv_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_chat_conv_chat_id ON chat_conversations (chat_id);
CREATE INDEX idx_chat_conv_user_id ON chat_conversations (user_id);
CREATE INDEX idx_chat_conv_guest_session ON chat_conversations (guest_session);
CREATE INDEX idx_chat_conv_status ON chat_conversations (status);

CREATE TABLE chat_messages
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    version         BIGINT      NOT NULL DEFAULT 0,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    conversation_id BIGINT      NOT NULL,
    role            VARCHAR(16) NOT NULL,
    content         TEXT        NOT NULL,
    tool_name       VARCHAR(128) NULL,
    tokens_used     INT NULL,
    CONSTRAINT fk_chat_msg_conv FOREIGN KEY (conversation_id) REFERENCES chat_conversations (id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_msg_conv_id ON chat_messages (conversation_id);
CREATE INDEX idx_chat_msg_role ON chat_messages (role);
