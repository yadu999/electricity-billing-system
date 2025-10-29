-- Run this script with a MySQL client (mysql or MySQL Workbench) to create the database and a dedicated user.
-- Replace 'ebilluser' and 's3cret' with your chosen username and password.

CREATE DATABASE IF NOT EXISTS `ebilling` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'ebilluser'@'localhost' IDENTIFIED BY 's3cret';
GRANT ALL PRIVILEGES ON `ebilling`.* TO 'ebilluser'@'localhost';
FLUSH PRIVILEGES;

-- Optionally, if you need remote access from other hosts change 'localhost' -> '%',
-- and ensure MySQL server is configured to accept remote connections.
