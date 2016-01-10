# --- !Ups

CREATE TABLE IF NOT EXISTS account(
    id_user SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    itunes_file_hash VARCHAR(32) DEFAULT NULL,
    spotify_token VARCHAR(255) DEFAULT NULL,
    deezer_token VARCHAR(256) DEFAULT NULL,
    lastfm_token VARCHAR(256) DEFAULT NULL,
    soundcloud_token VARCHAR(256) DEFAULT NULL
);

INSERT INTO account(name, password)
VALUES('jacke', 'a92f6bdb75789bccc118adfcf704029aa58063c604bab4fcdd9cd126ef9b69af');

CREATE TABLE IF NOT EXISTS artist(
    id_artist SERIAL PRIMARY KEY,
    artist_name VARCHAR(256) NOT NULL,
    spotify_id VARCHAR(32) DEFAULT NULL,
    soundcloud_id VARCHAR(32) DEFAULT NULL,
    deezer_id VARCHAR(32) DEFAULT NULL,
    lastfm_id VARCHAR(40) DEFAULT NULL,
    pic_url VARCHAR(256) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (artist_name)
);

CREATE TABLE IF NOT EXISTS album(
    id_album SERIAL PRIMARY KEY,
    album_name VARCHAR(256) NOT NULL,
    fk_artist INT NOT NULL REFERENCES artist(id_artist),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (album_name,fk_artist)
);

CREATE TABLE IF NOT EXISTS track(
    id_track SERIAL PRIMARY KEY,
    track_name VARCHAR(256) NOT NULL,
    fk_artist INT NOT NULL REFERENCES artist(id_artist),
    fk_album INT DEFAULT NULL REFERENCES album(id_album),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (track_name, fk_artist, fk_album)
);

CREATE TABLE IF NOT EXISTS user_collection(
    id_collection SERIAL PRIMARY KEY,
    fk_user INT DEFAULT NULL REFERENCES account(id_user),
    fk_track INT NOT NULL REFERENCES track(id_track),
    user_session VARCHAR(32) DEFAULT NULL,
    times_played INTEGER DEFAULT 1,
    imported_from VARCHAR(100) DEFAULT '',
    UNIQUE (fk_user, fk_track),
    UNIQUE (user_session, fk_track)
);

CREATE TABLE IF NOT EXISTS user_artist_liking(
    id_user_artist_liking SERIAL PRIMARY KEY,
    fk_user INT DEFAULT NULL REFERENCES account(id_user),
    user_session VARCHAR(32) DEFAULT NULL,
    fk_artist INT NOT NULL REFERENCES artist(id_artist),
    score REAL DEFAULT 1.0,
    UNIQUE (fk_user, fk_artist),
    UNIQUE (user_session, fk_artist)
);

# --- !Downs

DROP TABLE IF EXISTS album CASCADE;
DROP TABLE IF EXISTS account CASCADE;
DROP TABLE IF EXISTS artist CASCADE;
DROP TABLE IF EXISTS track CASCADE;
DROP TABLE IF EXISTS user_collection;
DROP TABLE IF EXISTS user_artist_liking;

