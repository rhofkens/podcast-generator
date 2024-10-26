ALTER TABLE podcasts
ADD COLUMN user_id VARCHAR(255) NOT NULL DEFAULT 'legacy';

-- Remove the default after adding the column
ALTER TABLE podcasts 
ALTER COLUMN user_id DROP DEFAULT;
