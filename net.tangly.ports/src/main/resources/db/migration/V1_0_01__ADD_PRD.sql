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

CREATE SCHEMA prd;

CREATE TABLE prd.products (
    oid         BIGINT PRIMARY KEY,
    id          VARCHAR(64) NOT NULL UNIQUE,
    name        VARCHAR(64),
    fromDate    DATE,
    toDate      DATE,
    text        CLOB,
    contractIds CLOB,
    tags        CLOB
);


CREATE TABLE prd.assignments (
    oid        BIGINT PRIMARY KEY,
    id         VARCHAR(64) NOT NULL UNIQUE,
    name       VARCHAR(64),
    fromDate   DATE,
    toDate     DATE,
    text       CLOB,
    productOid BIGINT      NOT NULL,
    employeeId VARCHAR(32) NOT NULL,
    tags       CLOB,
    CONSTRAINT assignmemts_productOid FOREIGN KEY (productOid) REFERENCES prd.products (oid)
);

CREATE TABLE prd.efforts (
    oid               BIGINT PRIMARY KEY,
    id                VARCHAR(64) NOT NULL UNIQUE,
    text              CLOB,
    startedOn         DATETIME,
    durationInMinutes INTEGER,
    assignmentOid     BIGINT,
    contractId        VARCHAR(32),
    FOREIGN KEY (assignmentOid) REFERENCES prd.assignments (oid)
);
