--
-- Datenbank: `tresordb`
--

DROP DATABASE IF EXISTS tresordb;
CREATE DATABASE tresordb;
USE tresordb;

-- --------------------------------------------------------

--
-- table user
--

CREATE TABLE user (
    id int NOT NULL AUTO_INCREMENT,
    first_name varchar(30) NOT NULL,
    last_name varchar(30) NOT NULL,
    email varchar(30) NOT NULL,
    password varchar(72) NOT NULL,
    user_salt varchar(48) NOT NULL,
    PRIMARY KEY (id)
);

--
-- table user content
--

INSERT INTO `user` (`id`, `first_name`, `last_name`, `email`, `password`, `user_salt`) VALUES
(1, 'Hans', 'Muster', 'hans.muster@bbw.ch', 'abcd', 'salt1'),
(2, 'Paula', 'Kuster', 'paula.kuster@bbw.ch', 'efgh', 'salt2'),
(3, 'Andrea', 'Oester', 'andrea.oester@bbw.ch', 'ijkl', 'salt3');

--
-- table secret
--

CREATE TABLE secret (
    id int NOT NULL AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    content TEXT NOT NULL,
    user_id int,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

--
-- table secret content
--

INSERT INTO `secret` (`id`, `title`, `content`, `user_id`) VALUES
    (1, 'Eragon', 'Und Eragon ging auf den Drachen zu.', 1),
    (2, 'Visa Card', '4242 4242 4242 4241', 1),
    (3, 'Note', 'Und Eragon ging auf den Drachen zu.', 1);
