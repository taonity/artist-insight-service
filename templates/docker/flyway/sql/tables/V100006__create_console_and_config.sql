ALTER TABLE spotify_user ADD COLUMN console_role VARCHAR(20) NOT NULL DEFAULT 'NONE' CHECK (console_role IN ('NONE', 'ADMIN', 'OWNER'));
CREATE TABLE audit_log (
 id VARCHAR(36) PRIMARY KEY, action VARCHAR(40) NOT NULL, target_type VARCHAR(40) NOT NULL,
 target_id VARCHAR(200) NOT NULL, actor_spotify_id VARCHAR NOT NULL, occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
 before_value VARCHAR(20000), after_value VARCHAR(20000)
);
CREATE INDEX audit_log_occurred_at_idx ON audit_log(occurred_at, id);
CREATE TABLE config_override (
 config_key VARCHAR(200) PRIMARY KEY, value_json VARCHAR(20000) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR NOT NULL
);
