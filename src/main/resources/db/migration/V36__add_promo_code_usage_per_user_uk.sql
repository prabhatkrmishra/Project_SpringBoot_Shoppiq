-- ============================================================
-- Shoppiq
-- V36__add_promo_code_usage_per_user_uk.sql
--
-- Adds a unique constraint on (promo_code_id, user_id) to the
-- promo_code_usage table.
--
-- Why:
--   Per-user usage limits (user_usage_limit on promo_codes)
--   were previously enforced only at the application layer,
--   leaving a gap where concurrent requests could both pass
--   the count check and insert usage records independently.
--   The unique constraint provides database-level enforcement
--   as the final line of defence; the service layer catches
--   the resulting integrity violation and translates it to a
--   domain-level exception.
-- ============================================================

ALTER TABLE promo_code_usage
    ADD CONSTRAINT uk_promo_usage_user
        UNIQUE (promo_code_id, user_id);
