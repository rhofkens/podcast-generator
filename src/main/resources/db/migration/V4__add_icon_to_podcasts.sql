ALTER TABLE podcasts
ADD COLUMN icon VARCHAR(255);

-- Copy existing icon_url values to icon if needed
UPDATE podcasts 
SET icon = icon_url
WHERE icon_url IS NOT NULL;
