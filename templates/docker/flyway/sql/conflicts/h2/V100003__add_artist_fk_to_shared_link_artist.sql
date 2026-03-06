-- First remove the primary key constraint
ALTER TABLE shared_link_artist
DROP CONSTRAINT constraint_1;

-- Then drop the id column
ALTER TABLE shared_link_artist
DROP COLUMN id;

