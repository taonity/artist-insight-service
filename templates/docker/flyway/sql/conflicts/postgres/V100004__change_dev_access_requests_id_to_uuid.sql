-- Drop the existing primary key and serial id column
ALTER TABLE development_access_requests
    DROP COLUMN id;

-- Add the new UUID id column as primary key
ALTER TABLE development_access_requests
    ADD COLUMN id UUID PRIMARY KEY DEFAULT gen_random_uuid();

