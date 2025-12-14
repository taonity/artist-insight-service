CREATE TABLE shared_link (
    id              UUID PRIMARY KEY,
    user_id         VARCHAR NOT NULL UNIQUE,
    share_code      VARCHAR(12) NOT NULL UNIQUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    
    CONSTRAINT shared_link_user_fk FOREIGN KEY (user_id) REFERENCES spotify_user(spotify_id) ON DELETE CASCADE
);

CREATE INDEX idx_shared_link_share_code ON shared_link(share_code);
CREATE INDEX idx_shared_link_expires_at ON shared_link(expires_at);

CREATE TABLE shared_link_artist (
    id              UUID PRIMARY KEY,
    shared_link_id  UUID NOT NULL,
    artist_id       VARCHAR NOT NULL,
    
    CONSTRAINT shared_link_artist_link_fk FOREIGN KEY (shared_link_id) REFERENCES shared_link(id) ON DELETE CASCADE,
    CONSTRAINT shared_link_artist_unique UNIQUE (shared_link_id, artist_id)
);

CREATE INDEX idx_shared_link_artist_link_id ON shared_link_artist(shared_link_id);
