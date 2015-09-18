# --- !Ups

CREATE TABLE IF NOT EXISTS account(
    id INT(6) UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR(64) NOT NULL,
    password VARCHAR(256) NOT NULL
);

# --- !Downs

DROP TABLE account;