INSERT INTO spotify_user (spotify_id, display_name, token_value, gpt_usages_left)
VALUES ('3126nx54y24ryqyza3qxcchi4wry', 'TestUser', 'BQBEgCMJfvo2C3wSpfOf64UvnYoqR-jiXEfR2i9OffAMmiyOkycbQnVS2bWMk530SiWueBZkEQj1HmtR-u-kH9K_6yA4AVkF9A8Nm5C_Ym4uTBegz_gKnzgFCqBp1k7Tpj-aM0pfcFjY1JMq9v8J-xFyDu81EwYWcYsMFOlf_ZFUYsJ0_Vfnb9YEtlXTmCsOVl3ZucjRDu2XP7WsmrserzY4Yc6vgDRkC7tcDQS05SSaz_Co8wPOh89tr2Mwcwab', 10);


INSERT INTO artist (artist_id, artist_name) VALUES ('21bOoXa6JISSaqYu2oYbWy', 'By The Spirits');
INSERT INTO artist (artist_id, artist_name) VALUES ('6A0sYtzqMUPBpHzVvEgOhA', 'PATHS');
INSERT INTO artist (artist_id, artist_name) VALUES ('4uFZsG1vXrPcvnZ4iSQyrx', 'C418');


INSERT INTO artist_genres (artist_id, genre) VALUES ('21bOoXa6JISSaqYu2oYbWy', 'Folk');
INSERT INTO artist_genres (artist_id, genre) VALUES ('21bOoXa6JISSaqYu2oYbWy', 'Neo-Folk');
INSERT INTO artist_genres (artist_id, genre) VALUES ('21bOoXa6JISSaqYu2oYbWy', 'Pagan Folk');
INSERT INTO artist_genres (artist_id, genre) VALUES ('21bOoXa6JISSaqYu2oYbWy', 'Dark Folk');

INSERT INTO artist_genres (artist_id, genre) VALUES ('6A0sYtzqMUPBpHzVvEgOhA', 'Electronic');
INSERT INTO artist_genres (artist_id, genre) VALUES ('6A0sYtzqMUPBpHzVvEgOhA', 'Indie Pop');

INSERT INTO artist_genres (artist_id, genre) VALUES ('4uFZsG1vXrPcvnZ4iSQyrx', 'Ambient');
INSERT INTO artist_genres (artist_id, genre) VALUES ('4uFZsG1vXrPcvnZ4iSQyrx', 'Electronic');
INSERT INTO artist_genres (artist_id, genre) VALUES ('4uFZsG1vXrPcvnZ4iSQyrx', 'Soundtrack');


INSERT INTO spotify_user_enriched_artists (spotify_id, artist_id) VALUES ('3126nx54y24ryqyza3qxcchi4wry', '21bOoXa6JISSaqYu2oYbWy');
INSERT INTO spotify_user_enriched_artists (spotify_id, artist_id) VALUES ('3126nx54y24ryqyza3qxcchi4wry', '6A0sYtzqMUPBpHzVvEgOhA');


INSERT INTO app_settings (id, global_gpt_usages_left)
VALUES (0, 100);