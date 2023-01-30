/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

CREATE SCHEMA tangly;

CREATE TABLE tangly.dbCode
(
    id      INT PRIMARY KEY,
    code    VARCHAR(64) NOT NULL,
    enabled BOOLEAN
);

INSERT INTO tangly.dbCode (id, code, enabled)
VALUES (0, 'CODE_TEST_0', TRUE);
INSERT INTO tangly.DbCode (id, code, enabled)
VALUES (1, 'CODE_TEST_1', TRUE);
INSERT INTO tangly.dbCode (id, code, enabled)
VALUES (2, 'CODE_TEST_2', TRUE);
INSERT INTO tangly.dbCode (id, code, enabled)
VALUES (3, 'CODE_TEST_3', TRUE);
INSERT INTO tangly.dbCode (id, code, enabled)
VALUES (4, 'CODE_TEST_4', TRUE);
