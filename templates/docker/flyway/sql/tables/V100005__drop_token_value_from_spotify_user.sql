-- Drop the legacy masked Spotify access-token column. The application no longer
-- persists any Spotify token-derived data with the user record.
ALTER TABLE spotify_user DROP COLUMN token_value;
