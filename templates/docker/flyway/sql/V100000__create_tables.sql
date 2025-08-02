CREATE TABLE IF NOT EXISTS spotify_user (
    spotify_id      VARCHAR PRIMARY KEY,
    display_name    VARCHAR NOT NULL,
    token_value     VARCHAR NOT NULL,
    gpt_usages_left INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS artist (
    artist_id VARCHAR PRIMARY KEY,
    artist_name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS artist_genres (
    artist_id   VARCHAR NOT NULL,
    genre       VARCHAR NOT NULL,

    CONSTRAINT artist_genres_pk PRIMARY KEY (artist_id, genre),
    CONSTRAINT artist_genres_artist_fk FOREIGN KEY (artist_id) REFERENCES artist(artist_id)
);

CREATE TABLE IF NOT EXISTS spotify_user_enriched_artists (
    spotify_id      VARCHAR NOT NULL,
    artist_id       VARCHAR NOT NULL,

    CONSTRAINT spotify_user_enriched_artists_pk PRIMARY KEY (spotify_id, artist_id),
    CONSTRAINT spotify_user_enriched_artists_user_fk FOREIGN KEY (spotify_id) REFERENCES spotify_user(spotify_id),
    CONSTRAINT spotify_user_enriched_artists_artist_fk FOREIGN KEY (artist_id) REFERENCES artist(artist_id)
);