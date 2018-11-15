CREATE SCHEMA tangly;

CREATE TABLE tangly.entityCode (
  id INT PRIMARY KEY,
  code VARCHAR(64),
  enabled BOOLEAN
);

CREATE TABLE tangly.entities (
  oid       BIGINT PRIMARY KEY,
  id        VARCHAR(64),
  name      VARCHAR(64),
  fromDate  DATE,
  toDate    DATE,
  text      CLOB,
  tags      CLOB,
  comments  CLOB,
  code      INT,
  owner     BIGINT,
  ownedBy   BIGINT,
  FOREIGN KEY (code) REFERENCES  tangly.entityCode(id),
  FOREIGN KEY (owner) REFERENCES tangly.entities (oid),
  FOREIGN KEY (ownedBy) REFERENCES tangly.entities (oid)
);

INSERT INTO tangly.entityCode (id, code, enabled) VALUES (0, 'CODE_TEST_0', TRUE);
INSERT INTO tangly.entityCode (id, code, enabled) VALUES (1, 'CODE_TEST_1', TRUE);
INSERT INTO tangly.entityCode (id, code, enabled) VALUES (2, 'CODE_TEST_2', TRUE);