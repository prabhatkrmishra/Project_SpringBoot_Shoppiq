-- Add review moderation status column
ALTER TABLE item_reviews
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' AFTER review;
