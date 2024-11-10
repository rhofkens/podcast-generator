-- First drop any existing foreign key constraints that are redundant
ALTER TABLE podcasts 
    DROP CONSTRAINT IF EXISTS fk_podcasts_context,
    DROP CONSTRAINT IF EXISTS fk_podcasts_transcript;

-- Add new columns to child tables before dropping the old ones
ALTER TABLE contexts
    ADD COLUMN IF NOT EXISTS podcast_id BIGINT;

ALTER TABLE transcripts  
    ADD COLUMN IF NOT EXISTS podcast_id BIGINT;

-- Migrate data if needed (assuming context_id and transcript_id exist in podcasts)
UPDATE contexts c
SET podcast_id = p.id
FROM podcasts p
WHERE p.context_id = c.id;

UPDATE transcripts t
SET podcast_id = p.id 
FROM podcasts p
WHERE p.transcript_id = t.id;

-- Remove redundant columns from podcasts table
ALTER TABLE podcasts 
    DROP COLUMN IF EXISTS context_id,
    DROP COLUMN IF EXISTS transcript_id;

-- Add NOT NULL constraints and foreign keys
ALTER TABLE contexts
    ALTER COLUMN podcast_id SET NOT NULL,
    ADD CONSTRAINT fk_contexts_podcast 
    FOREIGN KEY (podcast_id) REFERENCES podcasts(id);

ALTER TABLE transcripts
    ALTER COLUMN podcast_id SET NOT NULL,
    ADD CONSTRAINT fk_transcripts_podcast 
    FOREIGN KEY (podcast_id) REFERENCES podcasts(id);

-- Add constraints to existing tables if they don't exist
ALTER TABLE participants
    DROP CONSTRAINT IF EXISTS fk_participants_podcast,
    ADD CONSTRAINT fk_participants_podcast 
    FOREIGN KEY (podcast_id) REFERENCES podcasts(id);

ALTER TABLE audios
    DROP CONSTRAINT IF EXISTS fk_audios_podcast,
    ADD CONSTRAINT fk_audios_podcast 
    FOREIGN KEY (podcast_id) REFERENCES podcasts(id);
