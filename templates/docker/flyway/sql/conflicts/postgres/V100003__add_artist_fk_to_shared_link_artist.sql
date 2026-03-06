-- First remove the primary key constraint
ALTER TABLE shared_link_artist
DROP CONSTRAINT shared_link_artist_pkey;

-- Then drop the id column
ALTER TABLE shared_link_artist
DROP COLUMN id;

