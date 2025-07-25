CREATE TABLE IF NOT EXISTS spotify_user (
    spotify_id      VARCHAR NOT NULL,
	display_name    VARCHAR NOT NULL,
	token_value     VARCHAR NOT NULL,
	gpt_usages_left INTEGER NULL,

	CONSTRAINT spotify_user_pk PRIMARY KEY (display_name)
);

CREATE TABLE IF NOT EXISTS artist_genres (
    artist_name VARCHAR NOT NULL,
	genre       VARCHAR NOT NULL,

	CONSTRAINT artist_genres_pk PRIMARY KEY (artist_name, genre)
);

