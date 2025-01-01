CREATE TABLE voices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tags TEXT[], -- PostgreSQL array type for storing multiple tags
    external_voice_id VARCHAR(255) NOT NULL,
    voice_type VARCHAR(20) NOT NULL CHECK (voice_type IN ('STANDARD', 'GENERATED')),
    user_id VARCHAR(255),
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('male', 'female')),
    is_default BOOLEAN NOT NULL DEFAULT false,
    audio_preview_path VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (external_voice_id)
);

-- Create indexes for frequently queried columns
CREATE INDEX idx_voices_voice_type ON voices(voice_type);
CREATE INDEX idx_voices_user_id ON voices(user_id);
CREATE INDEX idx_voices_gender ON voices(gender);
CREATE INDEX idx_voices_tags ON voices USING gin(tags);

-- Add a comment to the table
COMMENT ON TABLE voices IS 'Stores voice profiles for podcast generation';

-- Add comments to key columns
COMMENT ON COLUMN voices.external_voice_id IS 'Reference to the voice ID in the elevenlabs service';
COMMENT ON COLUMN voices.voice_type IS 'Type of voice: STANDARD (built-in) or GENERATED (user-created)';
COMMENT ON COLUMN voices.tags IS 'Array of descriptive tags for voice categorization';
COMMENT ON COLUMN voices.is_default IS 'Indicates if this is a default voice option';
