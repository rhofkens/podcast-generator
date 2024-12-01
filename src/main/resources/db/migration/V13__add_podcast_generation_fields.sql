ALTER TABLE podcasts
ADD COLUMN generation_status VARCHAR(50),
ADD COLUMN generation_progress INTEGER,
ADD COLUMN generation_message TEXT;
