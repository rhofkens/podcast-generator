-- Create podcasts table
CREATE TABLE podcasts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    length INTEGER,
    status VARCHAR(50),
    icon_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create contexts table
CREATE TABLE contexts (
    id BIGSERIAL PRIMARY KEY,
    podcast_id BIGINT NOT NULL,
    description_text TEXT,
    source_url VARCHAR(255),
    file_path VARCHAR(255),
    processed_content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (podcast_id) REFERENCES podcasts(id) ON DELETE CASCADE
);

-- Create participants table
CREATE TABLE participants (
    id BIGSERIAL PRIMARY KEY,
    podcast_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(50),
    age INTEGER,
    role_description TEXT,
    voice_characteristics TEXT,
    synthetic_voice_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (podcast_id) REFERENCES podcasts(id) ON DELETE CASCADE
);

-- Create transcripts table
CREATE TABLE transcripts (
    id BIGSERIAL PRIMARY KEY,
    podcast_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    timing_info JSONB,
    edit_history JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (podcast_id) REFERENCES podcasts(id) ON DELETE CASCADE
);

-- Create audios table
CREATE TABLE audios (
    id BIGSERIAL PRIMARY KEY,
    podcast_id BIGINT NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    duration INTEGER,
    format VARCHAR(50),
    quality_metrics JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (podcast_id) REFERENCES podcasts(id) ON DELETE CASCADE
);

-- Create indexes for foreign keys and frequently accessed columns
CREATE INDEX idx_contexts_podcast_id ON contexts(podcast_id);
CREATE INDEX idx_participants_podcast_id ON participants(podcast_id);
CREATE INDEX idx_transcripts_podcast_id ON transcripts(podcast_id);
CREATE INDEX idx_audios_podcast_id ON audios(podcast_id);
CREATE INDEX idx_podcasts_created_at ON podcasts(created_at);
CREATE INDEX idx_podcasts_status ON podcasts(status);

-- Create trigger function for updating updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for all tables
CREATE TRIGGER update_podcasts_updated_at
    BEFORE UPDATE ON podcasts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_contexts_updated_at
    BEFORE UPDATE ON contexts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_participants_updated_at
    BEFORE UPDATE ON participants
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transcripts_updated_at
    BEFORE UPDATE ON transcripts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_audios_updated_at
    BEFORE UPDATE ON audios
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
