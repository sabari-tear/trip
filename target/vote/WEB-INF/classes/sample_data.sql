-- Reset tables
DELETE FROM `Group`;
DELETE FROM Vote;
DELETE FROM TripPlace;
DELETE FROM TripProposal;
DELETE FROM Student;
DELETE FROM Coordinator;
DELETE FROM Admin;
DELETE FROM Settings;

INSERT INTO Admin (username, password) VALUES ('admin', 'admin123');
INSERT INTO Settings (maxGroupSize) VALUES (4);
INSERT INTO Coordinator (username, password, department) VALUES
('coord_cs', 'pass123', 'Computer Science'),
('coord_business', 'pass123', 'Business'),
('coord_eng', 'pass123', 'Engineering');
INSERT INTO Student (name, username, password, department) VALUES
('Alice Johnson', 'alice_j', 'pass123', 'Computer Science'),
('Bob Smith', 'bob_s', 'pass123', 'Computer Science'),
('Charlie Brown', 'charlie_b', 'pass123', 'Computer Science'),
('David Wilson', 'david_w', 'pass123', 'Computer Science'),
('Eve Anderson', 'eve_a', 'pass123', 'Computer Science'),
('Frank Davis', 'frank_d', 'pass123', 'Computer Science'),
('Grace Taylor', 'grace_t', 'pass123', 'Computer Science'),
('Henry Martin', 'henry_m', 'pass123', 'Computer Science'),
('Ivy Clark', 'ivy_c', 'pass123', 'Computer Science'),
('Jack Lewis', 'jack_l', 'pass123', 'Computer Science');
INSERT INTO Student (name, username, password, department) VALUES
('Karen White', 'karen_w', 'pass123', 'Business'),
('Liam Moore', 'liam_m', 'pass123', 'Business'),
('Mary Johnson', 'mary_j', 'pass123', 'Business'),
('Nathan Brown', 'nathan_b', 'pass123', 'Business'),
('Olivia Davis', 'olivia_d', 'pass123', 'Business'),
('Peter Wilson', 'peter_w', 'pass123', 'Business'),
('Quinn Smith', 'quinn_s', 'pass123', 'Business'),
('Rachel Green', 'rachel_g', 'pass123', 'Business'),
('Sam Taylor', 'sam_t', 'pass123', 'Business'),
('Tom Harris', 'tom_h', 'pass123', 'Business');
INSERT INTO Student (name, username, password, department) VALUES
('Uma Patel', 'uma_p', 'pass123', 'Engineering'),
('Victor Lee', 'victor_l', 'pass123', 'Engineering'),
('Wendy Chen', 'wendy_c', 'pass123', 'Engineering'),
('Xavier Rodriguez', 'xavier_r', 'pass123', 'Engineering'),
('Yara Khan', 'yara_k', 'pass123', 'Engineering'),
('Zack Miller', 'zack_m', 'pass123', 'Engineering'),
('Amy Zhang', 'amy_z', 'pass123', 'Engineering'),
('Ben Thomas', 'ben_t', 'pass123', 'Engineering'),
('Cora Kim', 'cora_k', 'pass123', 'Engineering'),
('Dan Park', 'dan_p', 'pass123', 'Engineering');
INSERT INTO TripProposal (title, stage, isArchived) VALUES ('Summer Tech Conference 2025', 'ROUND1', false);
SET @proposal1_id = LAST_INSERT_ID();

INSERT INTO TripPlace (proposalId, placeName, votesRound1) VALUES
(@proposal1_id, 'San Francisco, CA', 5),
(@proposal1_id, 'Seattle, WA', 3),
(@proposal1_id, 'Austin, TX', 2),
(@proposal1_id, 'Boston, MA', 1),
(@proposal1_id, 'New York, NY', 4);
INSERT INTO TripProposal (title, stage, isArchived) VALUES ('Business Summit 2025', 'ROUND2', false);
SET @proposal2_id = LAST_INSERT_ID();
INSERT INTO TripPlace (proposalId, placeName, votesRound1, votesRound2) VALUES
(@proposal2_id, 'Chicago, IL', 15, 8),
(@proposal2_id, 'Miami, FL', 12, 7);
INSERT INTO TripProposal (title, stage, isArchived) VALUES ('Engineering Expo 2025', 'FINALIZED', false);
SET @proposal3_id = LAST_INSERT_ID();

INSERT INTO TripPlace (proposalId, placeName, votesRound1, votesRound2) VALUES
(@proposal3_id, 'Las Vegas, NV', 18, 12);

UPDATE TripProposal SET finalizedPlaceId = LAST_INSERT_ID() WHERE id = @proposal3_id;
INSERT INTO TripProposal (title, stage, isArchived) VALUES ('Past Conference 2024', 'FINALIZED', true);
SET @proposal4_id = LAST_INSERT_ID();

INSERT INTO TripPlace (proposalId, placeName, votesRound1, votesRound2) VALUES
(@proposal4_id, 'Denver, CO', 20, 15);

UPDATE TripProposal SET finalizedPlaceId = LAST_INSERT_ID() WHERE id = @proposal4_id;

-- Add some votes for ROUND1 proposal
INSERT INTO Vote (studentId, proposalId, placeId, round) 
SELECT 
    s.id,
    @proposal1_id,
    (SELECT id FROM TripPlace WHERE proposalId = @proposal1_id LIMIT 1),
    1
FROM Student s
WHERE s.department = 'Computer Science'
LIMIT 5;
INSERT INTO Vote (studentId, proposalId, placeId, round)
SELECT 
    s.id,
    @proposal2_id,
    (SELECT id FROM TripPlace WHERE proposalId = @proposal2_id LIMIT 1),
    2
FROM Student s
WHERE s.department = 'Business'
LIMIT 8;
INSERT INTO Vote (studentId, proposalId, placeId, round)
SELECT 
    s.id,
    @proposal3_id,
    (SELECT id FROM TripPlace WHERE proposalId = @proposal3_id LIMIT 1),
    2
FROM Student s
WHERE s.department = 'Engineering'
LIMIT 12;
INSERT INTO `Group` (proposalId, groupNumber, studentId)
SELECT 
    @proposal3_id,
    FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 4) + 1,
    s.id
FROM Student s
WHERE s.department = 'Engineering'
LIMIT 12;
INSERT INTO `Group` (proposalId, groupNumber, studentId)
SELECT 
    @proposal4_id,
    FLOOR((ROW_NUMBER() OVER (ORDER BY s.id) - 1) / 4) + 1,
    s.id
FROM Student s
WHERE s.department = 'Business'
LIMIT 15;
