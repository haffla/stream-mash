# --- !Ups

CREATE TABLE IF NOT EXISTS account(
    id_user INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR(64) NOT NULL,
    password VARCHAR(64) NOT NULL
);

INSERT INTO account(name, password)
VALUES('jacke', 'a92f6bdb75789bccc118adfcf704029aa58063c604bab4fcdd9cd126ef9b69af');

CREATE TABLE IF NOT EXISTS album(
    id_album INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    interpret VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    fk_user INT NOT NULL,
    FOREIGN KEY (fk_user) REFERENCES account(id_user) ON DELETE CASCADE ON UPDATE RESTRICT,
    UNIQUE INDEX name_interpret (name,interpret)
);

# --- !Downs

DROP TABLE IF EXISTS album;
DROP TABLE IF EXISTS account;

