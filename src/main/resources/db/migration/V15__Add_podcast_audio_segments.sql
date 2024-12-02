CREATE TABLE podcast_audio_segments (
    podcast_id BIGINT NOT NULL,
    segment_path VARCHAR(255) NOT NULL,
    segment_index INTEGER NOT NULL,
    CONSTRAINT fk_podcast_audio_segments_podcast 
        FOREIGN KEY (podcast_id) 
        REFERENCES podcasts(id) 
        ON DELETE CASCADE,
    PRIMARY KEY (podcast_id, segment_index)
);
