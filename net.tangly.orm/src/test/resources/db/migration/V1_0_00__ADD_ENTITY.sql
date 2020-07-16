/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

CREATE SCHEMA tangly;

CREATE TABLE tangly.entityCode
(
    id      INT PRIMARY KEY,
    code    VARCHAR(64) NOT NULL,
    enabled BOOLEAN
);

CREATE TABLE tangly.comments
(
    oid     BIGINT PRIMARY KEY,
    created DATETIME    NOT NULL,
    author  VARCHAR(64) NOT NULL,
    text    CLOB,
    tags    CLOB,
    ownedBy BIGINT      NOT NULL
);

CREATE TABLE tangly.entities
(
    oid         BIGINT PRIMARY KEY,
    id          VARCHAR(64),
    name        VARCHAR(64),
    fromDate    DATE,
    toDate      DATE,
    text        CLOB,
    tags        CLOB,
    code        INT,
    owner       BIGINT,
    ownedBy     BIGINT,
    jsonValues CLOB,
    FOREIGN KEY (code) REFERENCES tangly.entityCode (id),
    FOREIGN KEY (owner) REFERENCES tangly.entities (oid),
    FOREIGN KEY (ownedBy) REFERENCES tangly.entities (oid)
);

INSERT INTO tangly.entityCode (id, code, enabled)
VALUES (0, 'CODE_TEST_0', TRUE);
INSERT INTO tangly.entityCode (id, code, enabled)
VALUES (1, 'CODE_TEST_1', TRUE);
INSERT INTO tangly.entityCode (id, code, enabled)
VALUES (2, 'CODE_TEST_2', TRUE);