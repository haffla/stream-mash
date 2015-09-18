# --- !Ups

CREATE TABLE IF NOT EXISTS cat (name VARCHAR(32) NOT NULL PRIMARY KEY,
                  color VARCHAR(32) NOT NULL,
                  age INT(3) NOT NULL,
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);

# --- !Downs
DROP TABLE cat;