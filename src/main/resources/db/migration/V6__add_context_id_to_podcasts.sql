ALTER TABLE podcasts
ADD COLUMN context_id BIGINT;

ALTER TABLE podcasts
ADD CONSTRAINT fk_podcast_context
FOREIGN KEY (context_id) 
REFERENCES contexts(id);
