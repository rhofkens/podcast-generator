ALTER TABLE audios 
ADD COLUMN filename VARCHAR(255);

-- Copy existing file_path values to filename if needed
UPDATE audios 
SET filename = SUBSTRING(file_path FROM '[^/]*$')
WHERE file_path IS NOT NULL;
