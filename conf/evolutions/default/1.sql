# --- !Ups

CREATE TABLE IF NOT EXISTS account(
    id_user SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    itunes_file_hash VARCHAR(32) DEFAULT NULL,
    spotify_token VARCHAR(255) DEFAULT NULL,
    spotify_token_refresh VARCHAR(255) DEFAULT NULL,
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

INSERT INTO artist(artist_name) VALUES ('Nicolas Jaar');
INSERT INTO artist(artist_name) VALUES ('Four Tet');
INSERT INTO artist(artist_name) VALUES ('Radiohead');

CREATE TABLE IF NOT EXISTS album(
    id_album SERIAL PRIMARY KEY,
    album_name VARCHAR(256) NOT NULL,
    fk_artist INT NOT NULL REFERENCES artist(id_artist) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (album_name,fk_artist)
);

INSERT INTO album(album_name, fk_artist) VALUES ('Space Is Only Noise', 1);
INSERT INTO album(album_name, fk_artist) VALUES ('Beautiful Rewind', 2);
INSERT INTO album(album_name, fk_artist) VALUES ('The King of Limbs', 3);

CREATE TABLE IF NOT EXISTS track(
    id_track SERIAL PRIMARY KEY,
    track_name VARCHAR(256) NOT NULL,
    fk_artist INT NOT NULL REFERENCES artist(id_artist) ON DELETE CASCADE,
    fk_album INT DEFAULT NULL REFERENCES album(id_album) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (track_name, fk_artist, fk_album)
);

INSERT INTO track(track_name, fk_artist, fk_album) VALUES ('Colomb', 1, 1);
INSERT INTO track(track_name, fk_artist, fk_album) VALUES ('Gong', 2, 2);
INSERT INTO track(track_name, fk_artist, fk_album) VALUES ('Bloom', 3, 3);

CREATE TABLE IF NOT EXISTS user_collection(
    id_collection SERIAL PRIMARY KEY,
    fk_user INT DEFAULT NULL REFERENCES account(id_user) ON DELETE CASCADE,
    fk_track INT NOT NULL REFERENCES track(id_track) ON DELETE CASCADE,
    user_session VARCHAR(32) DEFAULT NULL,
    times_played INTEGER DEFAULT 1,
    imported_from VARCHAR(100) DEFAULT NULL,
    UNIQUE (fk_user, fk_track),
    UNIQUE (user_session, fk_track)
);

INSERT INTO user_collection(fk_user, fk_track, times_played) VALUES (1, 1, 25);
INSERT INTO user_collection(fk_user, fk_track, times_played) VALUES (1, 2, 77);
INSERT INTO user_collection(fk_user, fk_track, times_played) VALUES (1, 3, 46);

CREATE TABLE IF NOT EXISTS user_artist_liking(
    id_user_artist_liking SERIAL PRIMARY KEY,
    fk_user INT DEFAULT NULL REFERENCES account(id_user) ON DELETE CASCADE,
    user_session VARCHAR(32) DEFAULT NULL,
    fk_artist INT NOT NULL REFERENCES artist(id_artist) ON DELETE CASCADE,
    score REAL DEFAULT 1.0,
    UNIQUE (fk_user, fk_artist),
    UNIQUE (user_session, fk_artist)
);

CREATE TABLE IF NOT EXISTS spotify_artist(
    id_spotify_artist INT REFERENCES artist(id_artist) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS spotify_album(
    id_spotify_album INT REFERENCES album(id_album) ON DELETE CASCADE,
    spotify_id VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS service_artist_absence(
    id_service_artist_absence SERIAL PRIMARY KEY,
    fk_artist INT NOT NULL REFERENCES artist(id_artist) ON DELETE CASCADE,
    fk_user INT DEFAULT NULL REFERENCES account(id_user),
    user_session VARCHAR(32) DEFAULT NULL,
    service VARCHAR(32) NOT NULL,
    UNIQUE (fk_artist, fk_user, service),
    UNIQUE (fk_artist, user_session, service)
);

# --- !Downs

DROP TABLE IF EXISTS album CASCADE;
DROP TABLE IF EXISTS account CASCADE;
DROP TABLE IF EXISTS artist CASCADE;
DROP TABLE IF EXISTS track CASCADE;
DROP TABLE IF EXISTS user_collection CASCADE;
DROP TABLE IF EXISTS user_artist_liking CASCADE;
DROP TABLE IF EXISTS spotify_artist CASCADE;
DROP TABLE IF EXISTS spotify_album CASCADE;
DROP TABLE IF EXISTS service_artist_absence CASCADE;

