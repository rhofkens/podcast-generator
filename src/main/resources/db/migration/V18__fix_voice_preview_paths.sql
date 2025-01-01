-- Update audio preview paths to use external_voice_id in filenames
UPDATE voices 
SET audio_preview_path = '/voice-previews/' || external_voice_id || '.mp3'
WHERE voice_type = 'STANDARD';

-- Add a comment explaining this migration
COMMENT ON TABLE voices IS 'Updated standard voices to use external_voice_id in preview paths';
