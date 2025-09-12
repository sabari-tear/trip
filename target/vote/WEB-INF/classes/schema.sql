CREATE DATABASE IF NOT EXISTS trip_voting;
USE trip_voting;

CREATE TABLE IF NOT EXISTS Admin (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS Coordinator (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS Student (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS TripProposal (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    stage ENUM('ROUND1', 'ROUND2', 'FINALIZED') NOT NULL DEFAULT 'ROUND1',
    isArchived BOOLEAN DEFAULT FALSE,
    finalizedPlaceId INT
);

CREATE TABLE IF NOT EXISTS TripPlace (
    id INT PRIMARY KEY AUTO_INCREMENT,
    proposalId INT,
    placeName VARCHAR(200) NOT NULL,
    votesRound1 INT DEFAULT 0,
    votesRound2 INT DEFAULT 0,
    FOREIGN KEY (proposalId) REFERENCES TripProposal(id)
);

CREATE TABLE IF NOT EXISTS Vote (
    id INT PRIMARY KEY AUTO_INCREMENT,
    studentId INT,
    proposalId INT,
    placeId INT,
    round INT NOT NULL,
    FOREIGN KEY (studentId) REFERENCES Student(id),
    FOREIGN KEY (proposalId) REFERENCES TripProposal(id),
    FOREIGN KEY (placeId) REFERENCES TripPlace(id)
);

CREATE TABLE IF NOT EXISTS `Group` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    proposalId INT,
    groupNumber INT NOT NULL,
    studentId INT,
    FOREIGN KEY (proposalId) REFERENCES TripProposal(id),
    FOREIGN KEY (studentId) REFERENCES Student(id)
);

CREATE TABLE IF NOT EXISTS Settings (
    maxGroupSize INT NOT NULL DEFAULT 5
);

-- Insert default admin
INSERT INTO Admin (username, password) VALUES ('admin', 'admin123');

-- Insert sample settings
INSERT INTO Settings (maxGroupSize) VALUES (5);
