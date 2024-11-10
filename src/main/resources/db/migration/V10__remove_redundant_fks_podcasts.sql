-- First drop any existing foreign key constraints that are redundant
ALTER TABLE podcasts 
    DROP CONSTRAINT IF EXISTS fk_podcasts_context,
    DROP CONSTRAINT IF EXISTS fk_podcasts_transcript;

-- Remove redundant columns from podcasts table
ALTER TABLE podcasts 
    DROP COLUMN IF EXISTS context_id,
    DROP COLUMN IF EXISTS transcript_id;

-- Ensure child tables have correct foreign keys to podcast
ALTER TABLE contexts
    ADD COLUMN IF NOT EXISTS podcast_id BIGINT,
    ADD CONSTRAINT fk_contexts_podcast 
    FOREIGN KEY (podcast_id) REFERENCES podcasts(id);

ALTER TABLE transcripts
    ADD COLUMN IF NOT EXISTS podcast_id BIGINT,
    ADD CONSTRAINT fk_transcripts_podcast 
    FOREIGN KEY (podcast_id) REFERENCES podcasts(id);

-- participants and audios tables should already have podcast_id foreign keys
-- but let's verify they have the constraints
ALTER TABLE participants
    ADD CONSTRAINT IF NOT EXISTS fk_participants_podcast 
    FOREIGN KEY (podcast_id) REFERENCES podcasts(id);

ALTER TABLE audios
    ADD CONSTRAINT IF NOT EXISTS fk_audios_podcast 
    FOREIGN KEY (podcast_id) REFERENCES podcasts(id);
