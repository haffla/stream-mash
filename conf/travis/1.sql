CREATE TABLE IF NOT EXISTS account(
    id_user SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    password VARCHAR(64) NOT NULL,
    itunes_file_hash VARCHAR(32),
    spotify_token VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS album(
    id_album SERIAL,
    name VARCHAR(255) NOT NULL,
    interpret VARCHAR(255) NOT NULL,
    fk_user INT DEFAULT NULL REFERENCES account(id_user),
    user_session_key VARCHAR(32) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (name,interpret,fk_user),
    UNIQUE (name,interpret,user_session_key)
);