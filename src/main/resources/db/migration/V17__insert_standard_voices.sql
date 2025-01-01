-- Insert standard voices from the CSV data
INSERT INTO voices (
    name,
    tags,
    external_voice_id,
    voice_type,
    gender,
    is_default,
    audio_preview_path
) VALUES 
(
    'Daniel',
    ARRAY['News', 'British', 'Authorative', 'Middle-Aged', 'Male'],
    'onwK4e9ZLuTAKqWW03F9',
    'STANDARD',
    'male',
    true,
    '/voice-previews/daniel-preview.mp3'
),
(
    'J. Audiobook',
    ARRAY['Narrative & Story', 'British', 'Professional', 'Young', 'Male'],
    '4u5cJuSmHP9d6YRolsOu',
    'STANDARD',
    'male',
    true,
    '/voice-previews/j-audiobook-preview.mp3'
),
(
    'Alice',
    ARRAY['News', 'British', 'Confident', 'Middle-Aged', 'Female'],
    'Xb7hH8MSUJpSbSDYk0k2',
    'STANDARD',
    'female',
    true,
    '/voice-previews/alice-preview.mp3'
),
(
    'Matilda',
    ARRAY['Narrative', 'American', 'Friendly', 'Middle-Aged', 'Female'],
    'XrExE9yKIg1WjnnlVkGX',
    'STANDARD',
    'female',
    true,
    '/voice-previews/matilda-preview.mp3'
);

-- Add a comment explaining this migration
COMMENT ON TABLE voices IS 'Standard voices initialized from standard-voices-list.csv';
