CREATE TABLE development_access_requests (
    id              SERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    message         TEXT,
    ip_address      VARCHAR(45),
    user_agent      TEXT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status          VARCHAR(50)
);