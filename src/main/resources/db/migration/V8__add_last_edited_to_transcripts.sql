ALTER TABLE transcripts
ADD COLUMN last_edited TIMESTAMP;

UPDATE transcripts 
SET last_edited = updated_at 
WHERE last_edited IS NULL;
