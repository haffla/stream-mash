# --- !Ups

CREATE SEQUENCE s_account_id;

CREATE TABLE IF NOT EXISTS account(
    id_user BIGINT DEFAULT nextval('s_account_id') PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    password VARCHAR(64) NOT NULL,
    itunes_file_hash VARCHAR(32),
    spotify_token VARCHAR(256),
    deezer_token VARCHAR(256),
    rdio_token VARCHAR(256),
    lastfm_token VARCHAR(256),
    soundcloud_token VARCHAR(256)
);

INSERT INTO account(name, password)
VALUES('jacke', 'a92f6bdb75789bccc118adfcf704029aa58063c604bab4fcdd9cd126ef9b69af');

CREATE SEQUENCE s_artist_id;

CREATE TABLE IF NOT EXISTS artist(
    id_artist BIGINT DEFAULT nextval('s_artist_id') PRIMARY KEY,
    artist_name VARCHAR(256) NOT NULL,
    spotify_id VARCHAR(32) DEFAULT NULL,
    rdio_id VARCHAR(32) DEFAULT NULL,
    soundcloud_id VARCHAR(32) DEFAULT NULL,
    deezer_id VARCHAR(32) DEFAULT NULL,
    lastfm_id VARCHAR(40) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (artist_name,spotify_id),
    UNIQUE (artist_name,rdio_id),
    UNIQUE (artist_name,soundcloud_id),
    UNIQUE (artist_name,deezer_id),
    UNIQUE (artist_name,lastfm_id)
);

CREATE SEQUENCE s_album_id;

CREATE TABLE IF NOT EXISTS album(
    id_album BIGINT DEFAULT nextval('s_album_id') PRIMARY KEY,
    album_name VARCHAR(256) NOT NULL,
    fk_artist INT NOT NULL REFERENCES artist(id_artist),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (album_name,fk_artist)
);

CREATE SEQUENCE s_track_id;

CREATE TABLE IF NOT EXISTS track(
    id_track BIGINT DEFAULT nextval('s_track_id') PRIMARY KEY,
    track_name VARCHAR(256) NOT NULL,
    fk_artist INT NOT NULL REFERENCES artist(id_artist),
    fk_album INT DEFAULT NULL REFERENCES album(id_album),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (track_name, fk_artist, fk_album)
);

CREATE SEQUENCE s_user_collection_id;

CREATE TABLE IF NOT EXISTS user_collection(
    id_collection BIGINT DEFAULT nextval('s_user_collection_id') PRIMARY KEY,
    fk_user INT DEFAULT NULL REFERENCES account(id_user),
    fk_track INT NOT NULL REFERENCES track(id_track),
    user_session VARCHAR(32) DEFAULT NULL,
    UNIQUE (fk_user, fk_track),
    UNIQUE (fk_user, user_session)
);

# --- !Downs

DROP SEQUENCE IF EXISTS s_account_id CASCADE;
DROP SEQUENCE IF EXISTS s_album_id CASCADE;
DROP SEQUENCE IF EXISTS s_artist_id CASCADE;
DROP SEQUENCE IF EXISTS s_track_id CASCADE;
DROP SEQUENCE IF EXISTS s_user_collection_id CASCADE;
DROP TABLE IF EXISTS album CASCADE;
DROP TABLE IF EXISTS account CASCADE;
DROP TABLE IF EXISTS artist CASCADE;
DROP TABLE IF EXISTS track CASCADE;
DROP TABLE IF EXISTS user_collection;

