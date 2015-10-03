# --- !Ups

CREATE TABLE IF NOT EXISTS account(
    id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR(64) NOT NULL,
    password VARCHAR(64) NOT NULL
);

INSERT INTO account(name, password)
VALUES('jacke', 'a92f6bdb75789bccc118adfcf704029aa58063c604bab4fcdd9cd126ef9b69af');

CREATE TABLE IF NOT EXISTS user_collection(
    id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    fk_interpret INT NOT NULL,
    fk_album INT NOT NULL,
    fk_user INT NOT NULL,
    FOREIGN KEY (fk_interpret) REFERENCES interpret(id),
    FOREIGN KEY (fk_album) REFERENCES album(id),
    FOREIGN KEY (fk_user) REFERENCES account(id)
);

CREATE TABLE IF NOT EXISTS interpret(
    id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    INDEX interpret_name (name)
);

CREATE TABLE IF NOT EXISTS album(
    id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    interpret VARCHAR(255) NOT NULL,
    INDEX name_interpret (name,interpret)
);

# --- !Downs

DROP TABLE IF EXISTS account;