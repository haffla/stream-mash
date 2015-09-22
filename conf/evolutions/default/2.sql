# --- !Ups

CREATE TABLE IF NOT EXISTS account(
    id INT(6) PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR(64) NOT NULL,
    password VARCHAR(256) NOT NULL
);

INSERT INTO ACCOUNTS(name, password)
VALUES('jacke', 'a92f6bdb75789bccc118adfcf704029aa58063c604bab4fcdd9cd126ef9b69af');

# --- !Downs

DROP TABLE IF EXISTS account;