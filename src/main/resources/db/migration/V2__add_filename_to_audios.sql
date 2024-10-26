ALTER TABLE audios 
ADD COLUMN filename VARCHAR(255);

-- Copy existing file_path values to filename if needed
UPDATE audios 
SET filename = SPLIT_PART(file_path, '/', -1)
WHERE file_path IS NOT NULL;
