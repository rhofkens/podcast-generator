ALTER TABLE transcripts 
ALTER COLUMN content TYPE jsonb USING content::jsonb;
