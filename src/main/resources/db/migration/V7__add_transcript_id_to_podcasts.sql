ALTER TABLE podcasts
ADD COLUMN transcript_id BIGINT;

ALTER TABLE podcasts
ADD CONSTRAINT fk_podcast_transcript
FOREIGN KEY (transcript_id) 
REFERENCES transcripts(id);
