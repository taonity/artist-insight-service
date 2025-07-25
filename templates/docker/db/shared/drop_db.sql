-- TODO remove db name
UPDATE pg_database SET datallowconn = 'false' WHERE datname = 'artist_insight_service';

SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = 'artist_insight_service';

DROP DATABASE artist_insight_service;

UPDATE pg_database SET datallowconn = 'true' WHERE datname = 'artist_insight_service';



