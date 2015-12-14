# --- !Ups

CREATE TABLE IF NOT EXISTS account(
    id_user SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    password VARCHAR(64) NOT NULL,
    itunes_file_hash VARCHAR(32)
);

INSERT INTO account(name, password)
VALUES('jacke', 'a92f6bdb75789bccc118adfcf704029aa58063c604bab4fcdd9cd126ef9b69af');

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

CREATE TABLE IF NOT EXISTS artist(
    id_artist SERIAL,
    name VARCHAR(255) NOT NULL,
    spotify_id VARCHAR(32) DEFAULT NULL,
    rdio_id VARCHAR(32) DEFAULT NULL,
    soundcloud_id VARCHAR(32) DEFAULT NULL,
    deezer_id VARCHAR(32) DEFAULT NULL,
    UNIQUE (name,spotify_id),
    UNIQUE (name,rdio_id),
    UNIQUE (name,soundcloud_id),
    UNIQUE (name,deezer_id)
);

# --- !Downs

DROP TABLE IF EXISTS album;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS artist;

