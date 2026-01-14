-- Migration: Add url column to trips table
-- Version: 1
-- Description: Add optional url field for external links (e.g., Wongnai, etc.)

ALTER TABLE trips 
ADD COLUMN url TEXT;

-- Add comment for documentation
COMMENT ON COLUMN trips.url IS 'External URL for trip details (e.g., Wongnai article link)';
